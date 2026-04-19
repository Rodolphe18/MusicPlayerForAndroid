package com.francotte.contentproviderformusic.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Window
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.francotte.contentproviderformusic.repository.SongsFetcherRepository
import com.francotte.contentproviderformusic.service.MusicService
import com.francotte.contentproviderformusic.ui.composable.rememberMediaController
import com.francotte.contentproviderformusic.ui.state.MusicApp
import com.francotte.contentproviderformusic.ui.state.rememberMusicAppState
import com.francotte.contentproviderformusic.ui.theme.ContentProviderForMusicTheme
import com.francotte.contentproviderformusic.utils.MediaManager
import com.francotte.contentproviderformusic.utils.PermissionManager
import dagger.hilt.android.AndroidEntryPoint

@kotlin.OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel by viewModels<MainViewModel>()


    @OptIn(UnstableApi::class, ExperimentalMaterial3WindowSizeClassApi::class)
    @SuppressLint("StateFlowValueCalledInComposition")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissions()
        installSplashScreen().setKeepOnScreenCondition { mainViewModel.isLoading.value }
        enableEdgeToEdge()
        startService(Intent(this, MusicService::class.java))
        setContent {
            ContentProviderForMusicTheme {
                val permissionGranted by mainViewModel.permissionGranted.collectAsStateWithLifecycle()
                val controller = rememberMediaController(this)
                val musicAppState = rememberMusicAppState()
                //   HideNavigationBar(window)
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
                LaunchedEffect(permissionGranted, controller, SongsFetcherRepository.songs.size) {
                    if (permissionGranted && controller != null && SongsFetcherRepository.songs.isNotEmpty()) {
                        if (controller.mediaItemCount == 0) {
                            mainViewModel.playSelectedSong(0)
                        }
                    }
                }
                if (permissionGranted) {
                    MusicApp(mainViewModel,calculateWindowSizeClass(this))
                }
            }
        }
    }

    private fun requestPermissions() {
        val multiplePermissionsContract =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionsStatusMap ->
                if (!permissionsStatusMap.containsValue(false)) {
                    // all permissions are accepted
                    Toast.makeText(this, "all permissions are accepted", Toast.LENGTH_SHORT).show()
                    mainViewModel.updatePermissionStatus()
                } else {
                    Toast.makeText(this, "all permissions are not accepted", Toast.LENGTH_SHORT)
                        .show()
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
