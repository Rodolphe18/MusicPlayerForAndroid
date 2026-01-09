package com.francotte.contentproviderformusic.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import com.francotte.contentproviderformusic.repository.UserDataRepository
import com.francotte.contentproviderformusic.service.MusicService
import com.francotte.contentproviderformusic.ui.composable.CurrentSongBar
import com.francotte.contentproviderformusic.ui.composable.HomeScreen
import com.francotte.contentproviderformusic.ui.composable.SongScreen
import com.francotte.contentproviderformusic.ui.composable.rememberMediaController
import com.francotte.contentproviderformusic.ui.theme.ContentProviderForMusicTheme
import com.francotte.contentproviderformusic.utils.MediaManager
import com.francotte.contentproviderformusic.utils.PermissionManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel by viewModels<MainViewModel>()


    @OptIn(UnstableApi::class)
    @SuppressLint("StateFlowValueCalledInComposition")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissions()
        installSplashScreen().setKeepOnScreenCondition { mainViewModel.isLoading.value }
        enableEdgeToEdge()
        startService(
            Intent(this, MusicService::class.java)
        )
        setContent {
            ContentProviderForMusicTheme {
                val permissionGranted by mainViewModel.permissionGranted.collectAsStateWithLifecycle()
                val isPlaying by mainViewModel.isPlaying.collectAsStateWithLifecycle()
                val currentDuration by mainViewModel.currentDuration.collectAsStateWithLifecycle()
                val screenStatus by mainViewModel.screenStatus.collectAsStateWithLifecycle()
                val currentIndex by mainViewModel.currentIndex.collectAsStateWithLifecycle()
                val controller = rememberMediaController(this)
                LaunchedEffect(controller) {
                    controller?.let { mainViewModel.attachController(it) }
                }
                DisposableEffect(controller) {
                    onDispose { mainViewModel.detachController() }
                }
                LaunchedEffect(permissionGranted) {
                    if (permissionGranted) {
                        UserDataRepository.updateSongs(MediaManager.getUserSongs(this@MainActivity))
                    }
                }
                LaunchedEffect(permissionGranted, controller, UserDataRepository.songs.size) {
                    if (permissionGranted && controller != null && UserDataRepository.songs.isNotEmpty()) {
                        if (controller.mediaItemCount == 0) {
                            mainViewModel.playSelectedSong(0)
                        }
                    }
                }
                if (permissionGranted) {
                    when (screenStatus) {
                        ScreenStatus.MAIN_SCREEN -> {
                            if (intent.getStringExtra("open_song") != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(bottom = 62.dp)
                                ) {
                                    HomeScreen(UserDataRepository.songs, currentIndex) { index, song ->
                                        mainViewModel.playSelectedSong(index)  }
                                    if(UserDataRepository.songs.isNotEmpty()) {
                                        CurrentSongBar(
                                            Modifier.align(Alignment.BottomCenter),
                                            UserDataRepository.songs[currentIndex],
                                            isPlaying, {
                                                mainViewModel.prevSong()
                                            },
                                            {
                                                mainViewModel.nextSong()
                                            }, {
                                                mainViewModel.playPause()
                                            }, currentDuration,
                                            {
                                                mainViewModel.onSeekBarValueChanged(it)
                                            }, {
                                                mainViewModel.stopSong()
                                            }, {
                                                mainViewModel.screenStatus.value = ScreenStatus.SONG_SCREEN
                                            }
                                        )
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(bottom = 42.dp)
                                ) {
                                    HomeScreen(UserDataRepository.songs,currentIndex) { index, song ->
                                        mainViewModel.playSelectedSong(index)
                                    }
                                   if(UserDataRepository.songs.isNotEmpty()) {
                                       CurrentSongBar(
                                           Modifier.align(Alignment.BottomCenter),
                                           UserDataRepository.songs[currentIndex],
                                           isPlaying,
                                           {
                                               mainViewModel.prevSong()
                                           },
                                           {
                                               mainViewModel.nextSong()
                                           }, {
                                               mainViewModel.playPause()
                                           }, currentDuration,
                                           {
                                               mainViewModel.onSeekBarValueChanged(
                                                   it
                                               )
                                           }, { mainViewModel.stopSong() }, {
                                               mainViewModel.screenStatus.value =
                                                   ScreenStatus.SONG_SCREEN
                                           })
                                   }
                                }
                            }
                        }

                        ScreenStatus.SONG_SCREEN -> {
                            if (intent.getStringExtra("open_song") != null) {
                                UserDataRepository.songs[currentIndex].let { song ->
                                    SongScreen(song = song,
                                        isPlaying = isPlaying,
                                        onPrevious = { mainViewModel.prevSong() },
                                        onNext = { mainViewModel.nextSong() },
                                        onPlayPause = { mainViewModel.playPause() },
                                        sliderValue = currentDuration,
                                        onSliderValueChanged = {
                                            mainViewModel.onSeekBarValueChanged(
                                                it
                                            )
                                        },
                                        onNavigationClick = {
                                            mainViewModel.screenStatus.value =
                                                ScreenStatus.MAIN_SCREEN
                                        }, onVerticalDrag = {mainViewModel.screenStatus.value =
                                            ScreenStatus.MAIN_SCREEN})
                                    LaunchedEffect(Unit) { mainViewModel.playPause() }
                                }
                            } else {
                                SongScreen(song = UserDataRepository.songs[currentIndex],
                                    isPlaying = isPlaying,
                                    onPrevious = { mainViewModel.prevSong() },
                                    onNext = { mainViewModel.nextSong() },
                                    onPlayPause = { mainViewModel.playPause() },
                                    sliderValue = currentDuration,
                                    onSliderValueChanged = {
                                        mainViewModel.onSeekBarValueChanged(
                                            it
                                        )
                                    },
                                    onNavigationClick = {
                                        mainViewModel.screenStatus.value =
                                            ScreenStatus.MAIN_SCREEN
                                    }, onVerticalDrag = {
                                        mainViewModel.screenStatus.value =
                                            ScreenStatus.MAIN_SCREEN
                                    })
                            }
                        }

                    }
                }
            }
        }
    }

    private fun requestPermissions() {
        val multiplePermissionsContract = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionsStatusMap ->
            if (!permissionsStatusMap.containsValue(false)) {
                // all permissions are accepted
                Toast.makeText(this, "all permissions are accepted", Toast.LENGTH_SHORT).show()
                mainViewModel.updatePermissionStatus()
            } else {
                Toast.makeText(this, "all permissions are not accepted", Toast.LENGTH_SHORT).show()
            }
        }
        PermissionManager.requestRuntimePermission(multiplePermissionsContract)
    }

}




