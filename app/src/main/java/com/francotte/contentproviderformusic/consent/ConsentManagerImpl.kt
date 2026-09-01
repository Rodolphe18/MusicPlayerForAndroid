package com.francotte.contentproviderformusic.consent

import android.app.Activity
import android.content.Context
import android.util.Log
import com.francotte.contentproviderformusic.BuildConfig
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Hashed IDs des appareils physiques à traiter comme test devices pour le consentement
 * (debug uniquement). Récupère l'ID dans Logcat au 1er lancement — le SDK loggue :
 * « Use ConsentDebugSettings.Builder().addTestDeviceHashedId("XXXX") to set this as a debug device ».
 * Les émulateurs sont déjà des test devices et n'ont pas besoin d'y figurer.
 */
private val TEST_DEVICE_HASHED_IDS = listOf("A5668C9D1F2BD913300A4D46940BF2A3")

@Singleton
class ConsentManagerImpl @Inject constructor(@param:ApplicationContext private val appContext: Context) :
    ConsentManager {
    private val _state = MutableStateFlow<ConsentState>(ConsentState.Idle)
    override val state: StateFlow<ConsentState> = _state.asStateFlow()

    private val mutex = Mutex()
    private val mobileAdsInitialized = AtomicBoolean(false)

    private val consentInformation: ConsentInformation by lazy {
        UserMessagingPlatform.getConsentInformation(appContext)
    }

    /** Initialise le SDK Ads une seule fois, hors thread principal (opération I/O). */
    private suspend fun initializeMobileAdsOnce() {
        if (!mobileAdsInitialized.compareAndSet(false, true)) return
        withContext(Dispatchers.IO) {
            suspendCancellableCoroutine { cont ->
                MobileAds.initialize(appContext) { if (cont.isActive) cont.resume(Unit) }
            }
        }
    }

    /**
     * Lit canRequestAds en garantissant que le SDK Ads est initialisé AVANT de renvoyer
     * true. L'UI compose la bannière sur ce signal : l'inverser exposerait à une requête
     * publicitaire émise avant l'initialisation.
     */
    private suspend fun resolveCanRequestAds(): Boolean {
        val canRequestAds = consentInformation.canRequestAds()
        if (canRequestAds) initializeMobileAdsOnce()
        return canRequestAds
    }

    suspend fun updateConsentInfo(activity: Activity): ConsentState =
        mutex.withLock {
            val current = _state.value
            if (current is ConsentState.Ready) return current

            _state.value = ConsentState.Loading

            val paramsBuilder =
                ConsentRequestParameters
                    .Builder()
                    .setTagForUnderAgeOfConsent(false)

            // En debug uniquement : force la géographie EEE pour voir le formulaire de
            // consentement même hors zone GDPR. Nécessite d'enregistrer l'appareil comme
            // test device via son hashed ID (voir TEST_DEVICE_HASHED_IDS). Les émulateurs
            // sont déjà des test devices ; sur device physique, renseigne le hashed ID
            // affiché dans Logcat au 1er lancement.
            if (BuildConfig.DEBUG) {
                val debugSettings =
                    ConsentDebugSettings
                        .Builder(appContext)
                        .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                        .apply { TEST_DEVICE_HASHED_IDS.forEach { addTestDeviceHashedId(it) } }
                        .build()
                paramsBuilder.setConsentDebugSettings(debugSettings)
            }

            val params = paramsBuilder.build()

            var failed = false
            withContext(Dispatchers.Main.immediate) {
                try {
                    activity.requestConsentInfoUpdateSuspend(consentInformation, params)
                } catch (t: Throwable) {
                    failed = true
                    Log.e("DEBUG_UMP", "requestConsentInfoUpdate FAILED", t)
                }
                Log.d(
                    "DEBUG_UMP",
                    "status=${consentInformation.consentStatus} canRequestAds=${consentInformation.canRequestAds()} privacyOptions=${consentInformation.privacyOptionsRequirementStatus}",
                )
            }

            if (failed) {
                // Sans réponse du CMP, canRequestAds() vaut true par défaut (statut UNKNOWN) :
                // s'y fier reviendrait à diffuser des annonces sans consentement. On refuse.
                // L'état n'est volontairement PAS mis en cache, pour qu'une panne transitoire
                // soit réessayée au prochain appel plutôt que de bloquer toute la session.
                _state.value = ConsentState.Idle
                return ConsentState.Failed
            }

            val ready = ConsentState.Ready(canRequestAds = resolveCanRequestAds())
            _state.value = ready
            ready
        }

    suspend fun showConsentIfRequired(activity: Activity) {
        withContext(Dispatchers.Main.immediate) {
            activity.showConsentFormIfRequiredSuspend()
        }
        val canRequestAds = resolveCanRequestAds()
        mutex.withLock {
            val current = _state.value
            if (current is ConsentState.Ready) {
                _state.value = current.copy(canRequestAds = canRequestAds)
            }
        }
    }

    override suspend fun ensureConsent(activity: Activity): Boolean {
        // Ready(canRequestAds = false) n'est PAS un échec : c'est « consentement requis »,
        // et c'est justement showConsentIfRequired qui affiche le formulaire. Seul Failed
        // (CMP injoignable) doit couper les annonces.
        if (updateConsentInfo(activity) is ConsentState.Failed) return false
        showConsentIfRequired(activity)
        return consentInformation.canRequestAds()
    }

    override fun isPrivacyOptionsRequired(): Boolean =
        consentInformation.privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    override suspend fun showPrivacyOptions(activity: Activity) {
        withContext(Dispatchers.Main.immediate) {
            suspendCancellableCoroutine<Unit> { cont ->
                UserMessagingPlatform.showPrivacyOptionsForm(activity) { _ ->
                    if (cont.isActive) cont.resume(Unit)
                }
                cont.invokeOnCancellation { /* no-op */ }
            }
        }
        val canRequestAds = resolveCanRequestAds()
        mutex.withLock {
            val current = _state.value
            if (current is ConsentState.Ready) {
                _state.value = current.copy(canRequestAds = canRequestAds)
            }
        }
    }

    /**
     * Avec l'UMP, le SDK Google Mobile Ads lit lui-même la chaîne de consentement TCF et
     * choisit annonces personnalisées ou non. L'extra « npa=1 » est l'approche pré-UMP et
     * ferait doublon. À n'appeler qu'après avoir vérifié canRequestAds().
     */
    override fun buildAdRequest(): AdRequest = AdRequest.Builder().build()

    fun resetForDebug() {
        consentInformation.reset()
        _state.value = ConsentState.Idle
    }
}

private suspend fun Activity.requestConsentInfoUpdateSuspend(
    consentInformation: ConsentInformation,
    params: ConsentRequestParameters,
) = suspendCancellableCoroutine<Unit> { cont ->
    consentInformation.requestConsentInfoUpdate(
        this,
        params,
        { if (cont.isActive) cont.resume(Unit) },
        { err ->
            if (cont.isActive) cont.resumeWithException(RuntimeException(err.message))
        },
    )
}

/** suspend wrapper : loadAndShowConsentFormIfRequired */
private suspend fun Activity.showConsentFormIfRequiredSuspend() =
    suspendCancellableCoroutine { cont ->
        UserMessagingPlatform.loadAndShowConsentFormIfRequired(this) { _ ->
            if (cont.isActive) cont.resume(Unit)
        }
        cont.invokeOnCancellation { /* no-op */ }
    }
