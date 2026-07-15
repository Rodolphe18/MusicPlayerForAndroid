package com.francotte.contentproviderformusic.ads

import android.app.Activity
import com.francotte.contentproviderformusic.consent.ConsentManager
import com.google.android.gms.ads.LoadAdError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

sealed interface InterstitialAdState {
    data object Idle : InterstitialAdState
    data object Loading : InterstitialAdState
    data object Timeout : InterstitialAdState
    data class FailedToLoad(val error: LoadAdError) : InterstitialAdState
    data object Showing : InterstitialAdState
    data object Shown : InterstitialAdState
    data object Clicked : InterstitialAdState
    data object Done : InterstitialAdState
}

@Singleton
class InterstitialManager @Inject constructor(
    private val consentManager: ConsentManager,
) {
    private var hasShownAd = false
    private val adMutex = Mutex()

    fun loadAndShowInterstitialAd(activity: Activity): Flow<InterstitialAdState> =
        channelFlow {
            fun sendState(state: InterstitialAdState) {
                trySend(state).isSuccess
            }

            adMutex.withLock {
                if (hasShownAd) {
                    sendState(InterstitialAdState.Done)
                    return@withLock
                }

                sendState(InterstitialAdState.Loading)

                val canRequestAds = consentManager.ensureConsent(activity)
                if (!canRequestAds) {
                    sendState(InterstitialAdState.Done)
                    return@withLock
                }

                val adRequest = consentManager.buildAdRequest()

                val loadResultOrNull =
                    withTimeoutOrNull(3000) {
                        withContext(Dispatchers.Main.immediate) {
                            activity.loadInterstitialAd("ca-app-pub-8828725570000941/7675022600", adRequest)
                        }
                    }

                if (loadResultOrNull == null) {
                    sendState(InterstitialAdState.Timeout)
                    sendState(InterstitialAdState.Done)
                    return@withLock
                }

                when (loadResultOrNull) {
                    is InterstitialLoadResult.Failed -> {
                        sendState(InterstitialAdState.FailedToLoad(loadResultOrNull.error))
                        sendState(InterstitialAdState.Done)
                        return@withLock
                    }

                    is InterstitialLoadResult.Success -> {

                        hasShownAd = true
                        sendState(InterstitialAdState.Showing)

                        withContext(Dispatchers.Main.immediate) {
                            loadResultOrNull.ad
                                .showInterstitialAd(activity)
                                .collect { event ->
                                    when (event) {
                                        InterstitialShowEvent.Shown -> sendState(InterstitialAdState.Shown)
                                        InterstitialShowEvent.Clicked -> sendState(InterstitialAdState.Clicked)
                                        InterstitialShowEvent.Closed -> Unit
                                        is InterstitialShowEvent.FailedToShow -> Unit
                                    }
                                }
                        }

                        sendState(InterstitialAdState.Done)
                    }
                }
            }
        }
}
