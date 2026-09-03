package com.francotte.contentproviderformusic.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Window
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalView
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import com.francotte.contentproviderformusic.R
import com.francotte.contentproviderformusic.ads.InterstitialAdState
import com.francotte.contentproviderformusic.ads.InterstitialManager
import com.francotte.contentproviderformusic.repository.SongsFetcherRepository
import com.francotte.contentproviderformusic.service.MusicService
import com.francotte.contentproviderformusic.ui.composable.PermissionRequiredScreen
import com.francotte.contentproviderformusic.ui.composable.rememberMediaController
import com.francotte.contentproviderformusic.ui.state.MusicApp
import com.francotte.contentproviderformusic.ui.state.rememberMusicAppState
import com.francotte.contentproviderformusic.ui.theme.ContentProviderForMusicTheme
import com.francotte.contentproviderformusic.utils.MediaManager
import com.francotte.contentproviderformusic.utils.PermissionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@kotlin.OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel by viewModels<MainViewModel>()

    @Inject
    lateinit var interstitialManager: InterstitialManager


    @OptIn(UnstableApi::class, ExperimentalMaterial3WindowSizeClassApi::class)
    @SuppressLint("StateFlowValueCalledInComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissions()
        installSplashScreen().setKeepOnScreenCondition {
          mainViewModel.isLoading.value || mainViewModel.splashHold.value
        }
        // Barre de navigation translucide (pas de scrim blanc) : le contenu passe dessous.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            ),
            navigationBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            ),
        )
        startService(Intent(this, MusicService::class.java))
        setContent {
            ContentProviderForMusicTheme {
                val permissionGranted by mainViewModel.permissionGranted.collectAsStateWithLifecycle()
                val autoPlayOnStartup by mainViewModel.autoPlayOnStartup.collectAsStateWithLifecycle()
                val controller = rememberMediaController(this)
                val musicAppState = rememberMusicAppState()
                // Passe à true quand l'interstitiel est terminé (affiché, ignoré, en échec ou
                // en timeout). L'auto-play attend cet état pour ne pas démarrer sous l'annonce.
                var adFinished by remember { mutableStateOf(false) }
                // Masque la barre de navigation système du bas (réapparaît par swipe transitoire).
                HideNavigationBar(window)
                LaunchedEffect(controller) {
                    controller?.let { mainViewModel.attachController(it) }
                }
                DisposableEffect(controller) {
                    onDispose { mainViewModel.detachController() }
                }
                LaunchedEffect(permissionGranted) {
                    if (permissionGranted) {
                        SongsFetcherRepository.updateSongs(MediaManager.getUserSongs(this@MainActivity))
                    }
                }
                // Interstitiel au démarrage : seulement une fois la permission accordée
                // (expérience normale). Le splash reste affiché jusqu'à ce que l'annonce
                // apparaisse (Showing) ou soit résolue (Done : échec/refus/timeout), avec un
                // filet de 6 s. Le manager gère consentement et "une seule fois".
                LaunchedEffect(permissionGranted) {
                    if (permissionGranted) {
                        // Filet : si l'annonce n'apparaît jamais, on relâche le splash au bout de 6 s
                        // (ne touche pas à l'auto-play tant qu'une annonce peut encore s'afficher).
                        val splashTimeout = launch {
                            delay(6000)
                            mainViewModel.releaseSplash()
                        }
                        interstitialManager
                            .loadAndShowInterstitialAd(this@MainActivity)
                            .collect { state ->
                                when (state) {
                                    // Shown = l'annonce couvre RÉELLEMENT l'écran
                                    // (onAdShowedFullScreenContent). On retire le splash derrière
                                    // seulement ici — pas sur Showing, qui est émis AVANT show()
                                    // et laisserait apparaître l'écran principal ~0,5 s.
                                    InterstitialAdState.Shown -> mainViewModel.releaseSplash()
                                    // Terminé (annonce fermée, ou aucune annonce à afficher).
                                    InterstitialAdState.Done -> {
                                        adFinished = true
                                        mainViewModel.releaseSplash()
                                    }
                                    else -> Unit
                                }
                            }
                        // Filet de sécurité : si le flow se termine sans émettre Done.
                        splashTimeout.cancel()
                        adFinished = true
                        mainViewModel.releaseSplash()
                    }
                }
                LaunchedEffect(permissionGranted, adFinished, autoPlayOnStartup, controller, SongsFetcherRepository.songs.size) {
                    if (permissionGranted && adFinished && autoPlayOnStartup && controller != null && SongsFetcherRepository.songs.isNotEmpty()) {
                        // Démarre la 1re chanson une fois l'interstitiel terminé, si l'utilisateur
                        // n'a pas désactivé l'auto-play et si rien n'est déjà en lecture (couvre
                        // aussi la réouverture à chaud où la file est déjà chargée).
                        if (!controller.isPlaying) {
                            mainViewModel.playSelectedSong(0)
                        }
                    }
                }
                if (permissionGranted) {
                    MusicApp(mainViewModel,calculateWindowSizeClass(this))
                } else {
                    // Toujours afficher quelque chose : sans cette branche, un refus
                    // laissait l'utilisateur devant un ecran blanc definitif.
                    PermissionRequiredScreen(onOpenSettings = ::openAppSettings)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // L'utilisateur a pu accorder (ou revoquer) l'acces depuis les reglages Android
        // pendant que l'app etait en arriere-plan : le resultat du dialogue seul ne suffit pas.
        mainViewModel.updatePermissionStatus(PermissionManager.hasAudioPermission(this))
    }

    /** Ouvre la fiche de l'app dans les reglages, seul endroit ou reactiver une permission refusee. */
    private fun openAppSettings() {
        startActivity(
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", packageName, null),
            )
        )
    }

    private fun requestPermissions() {
        // Etat avant la demande : le contrat rend la main sans afficher de dialogue
        // quand tout est deja accorde, son resultat ne dit donc pas si quelque chose
        // vient reellement d'etre accorde. Sans ce temoin, le message de confirmation
        // se rejouait a chaque lancement et a chaque rotation (onCreate relance la
        // demande). Voir PermissionManager.shouldAnnounceGrant.
        val grantedBefore = PermissionManager.hasAllPermissions(this)
        val multiplePermissionsContract =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { _ ->
                // On ne teste plus "toutes accordees" : refuser les notifications ne doit
                // pas masquer la bibliotheque. Seul l'acces audio est determinant, et on
                // lit l'etat systeme plutot que la reponse du dialogue.
                val granted = PermissionManager.hasAudioPermission(this)
                mainViewModel.updatePermissionStatus(granted)
                if (!granted) {
                    // Aucune annonce sans permission : on relache le splash pour laisser
                    // apparaitre l'ecran d'explication.
                    mainViewModel.releaseSplash()
                }
                val grantedNow = PermissionManager.hasAllPermissions(this)
                if (PermissionManager.shouldAnnounceGrant(grantedBefore, grantedNow)) {
                    Toast.makeText(
                        this,
                        getString(R.string.permissions_granted),
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        PermissionManager.requestRuntimePermission(multiplePermissionsContract)
    }

}


@Composable
fun HideNavigationBar(window: Window) {
    val view = LocalView.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    DisposableEffect(window, view, lifecycle) {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val controller = WindowCompat.getInsetsController(window, view)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                controller.hide(WindowInsetsCompat.Type.navigationBars())
            }
        }
        lifecycle.addObserver(observer)

        controller.hide(WindowInsetsCompat.Type.navigationBars())

        onDispose {
            lifecycle.removeObserver(observer)
            controller.show(WindowInsetsCompat.Type.navigationBars())
        }
    }
}
