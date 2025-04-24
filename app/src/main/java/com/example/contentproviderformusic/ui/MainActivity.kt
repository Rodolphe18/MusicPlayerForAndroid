package com.example.contentproviderformusic.ui

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.contentproviderformusic.R
import com.example.contentproviderformusic.ui.composable.CurrentSongBar
import com.example.contentproviderformusic.ui.composable.MainScreen
import com.example.contentproviderformusic.ui.composable.SongScreen
import com.example.contentproviderformusic.repository.UserDataRepository
import com.example.contentproviderformusic.service.MainService
import com.example.contentproviderformusic.ui.theme.ContentProviderForMusicTheme
import com.example.contentproviderformusic.utils.MediaManager
import com.example.contentproviderformusic.utils.PermissionManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

class MainActivity : ComponentActivity(), ServiceConnection {

    private var musicService: MainService? = null

    private val mainViewModel by viewModels<MainViewModel>()


    @SuppressLint("StateFlowValueCalledInComposition")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().setKeepOnScreenCondition { mainViewModel.isLoading.value }
        enableEdgeToEdge()
        setContent {
            ContentProviderForMusicTheme {
                val permissionGranted by mainViewModel.permissionGranted.collectAsState(false)
                val isPlaying by mainViewModel.isPlaying.collectAsStateWithLifecycle()
                val currentDuration by mainViewModel.currentDuration.collectAsStateWithLifecycle()
                val currentSong by mainViewModel.currentSong.collectAsStateWithLifecycle()
                val screenStatus by mainViewModel.screenStatus.collectAsStateWithLifecycle()
                PermissionManager.requestRuntimePermission(this)
                LaunchedEffect(musicService == null) {
                    if (musicService == null) {
                        initService()
                    }
                }
                LaunchedEffect(musicService == null) {
                    if (musicService == null) {
                        UserDataRepository.updateSongs(MediaManager.getUserSongs(this@MainActivity))
                    }
                }
                if (permissionGranted || PermissionManager.requestRuntimePermission(this)) {
                    when (screenStatus) {
                        ScreenStatus.MAIN_SCREEN -> {
                            if (intent.getStringExtra("open_song") != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(bottom = 62.dp)
                                ) {
                                    MainScreen(UserDataRepository.songs) { index, song ->
                                        musicService?.screenStatus?.value = ScreenStatus.SONG_SCREEN
                                        musicService?.playSelectedSong(song)
                                        musicService?.indexSong?.update { AtomicInteger(index) }
                                        musicService?.startCustomForegroundService(
                                            song,
                                            R.drawable.pause_icon
                                        )
                                    }
                                    currentSong?.let { song ->
                                        CurrentSongBar(
                                            Modifier.align(Alignment.BottomCenter),
                                            song,
                                            isPlaying, {
                                                musicService?.prevSong()
                                            },
                                            {
                                                musicService?.nextSong()
                                            }, {
                                                if (isPlaying) musicService?.pauseMusic() else musicService?.playMusic()
                                            }, currentDuration!!,
                                            {
                                                musicService?.onSeekBarValueChanged(
                                                    it
                                                )
                                            }, {
                                                musicService?.screenStatus?.value =
                                                    ScreenStatus.SONG_SCREEN
                                            }, {
                                                musicService?.stopSong()
                                            }, {musicService?.screenStatus?.value =
                                                ScreenStatus.SONG_SCREEN}
                                            )
                                    }
                                }
                                LaunchedEffect(Unit) { if (isPlaying) musicService?.playMusic() else musicService?.pauseMusic() }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(bottom = 42.dp)
                                ) {
                                    MainScreen(UserDataRepository.songs) { index, song ->
                                        musicService?.screenStatus?.value = ScreenStatus.SONG_SCREEN
                                        musicService?.playSelectedSong(song)
                                        musicService?.indexSong?.update { AtomicInteger(index) }
                                        musicService?.startCustomForegroundService(
                                            song,
                                            R.drawable.pause_icon
                                        )
                                    }
                                    currentSong?.let { song ->
                                        CurrentSongBar(
                                            Modifier.align(Alignment.BottomCenter),
                                            song,
                                            isPlaying,
                                            {
                                                musicService?.prevSong()
                                            },
                                            {
                                                musicService?.nextSong()
                                            }, {
                                                if (isPlaying) musicService?.pauseMusic() else musicService?.playMusic()
                                            }, currentDuration!!,
                                            {
                                                musicService?.onSeekBarValueChanged(
                                                    it
                                                )
                                            }, {
                                                musicService?.screenStatus?.value =
                                                    ScreenStatus.SONG_SCREEN
                                            }, { musicService?.stopSong()},{musicService?.screenStatus?.value =
                                                ScreenStatus.SONG_SCREEN})
                                    }

                                }
                            }
                        }

                        ScreenStatus.SONG_SCREEN -> {
                            if (intent.getStringExtra("open_song") != null) {
                                currentSong?.let { song ->
                                    SongScreen(song = song,
                                        isPlaying = isPlaying,
                                        onPrevious = { musicService?.prevSong() },
                                        onNext = { musicService?.nextSong() },
                                        onPlayPause = { if (isPlaying) musicService?.pauseMusic() else musicService?.playMusic() },
                                        sliderValue = currentDuration!!,
                                        onSliderValueChanged = {
                                            musicService?.onSeekBarValueChanged(
                                                it
                                            )
                                        },
                                        onNavigationClick = {
                                            musicService?.screenStatus?.value =
                                                ScreenStatus.MAIN_SCREEN
                                        }, onVerticalDrag = {musicService?.screenStatus?.value =
                                            ScreenStatus.MAIN_SCREEN})
                                    LaunchedEffect(Unit) { if (isPlaying) musicService?.playMusic() else musicService?.pauseMusic() }
                                }
                            } else {
                                currentSong?.let { song ->
                                    SongScreen(song = song,
                                        isPlaying = isPlaying,
                                        onPrevious = { musicService?.prevSong() },
                                        onNext = { musicService?.nextSong() },
                                        onPlayPause = { if (isPlaying) musicService?.pauseMusic() else musicService?.playMusic() },
                                        sliderValue = currentDuration!!,
                                        onSliderValueChanged = {
                                            musicService?.onSeekBarValueChanged(
                                                it
                                            )
                                        },
                                        onNavigationClick = {
                                            musicService?.screenStatus?.value =
                                                ScreenStatus.MAIN_SCREEN
                                        }, onVerticalDrag = {
                                            musicService?.screenStatus?.value =
                                                ScreenStatus.MAIN_SCREEN
                                        })
                                }
                            }
                        }

                    }
                }
            }
        }
    }


    private fun initService() {
        val intent = Intent(this, MainService::class.java)
        startService(intent)
        bindService(intent, this, BIND_AUTO_CREATE)
        mainViewModel.isLoading.value = false
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        if (musicService == null) {
            musicService = (service as MainService.MyBinder).currentService()
            musicService?.mAudioManager = getSystemService(AUDIO_SERVICE) as AudioManager
            musicService?.mAudioManager?.requestAudioFocus(
                musicService,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
        lifecycleScope.launch {
            (service as MainService.MyBinder).isPlaying().collectLatest {
                mainViewModel.isPlaying.value = it
            }
        }
        lifecycleScope.launch {
            (service as MainService.MyBinder).currentDuration().collectLatest {
                mainViewModel.currentDuration.value = it
            }
        }
        lifecycleScope.launch {
            (service as MainService.MyBinder).getCurrentSong().collectLatest {
                mainViewModel.currentSong.value = it
            }
        }
        lifecycleScope.launch {
            (service as MainService.MyBinder).getScreenStatus().collectLatest {
                mainViewModel.screenStatus.value = it
            }
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null
    }


    @Deprecated("the new method doesn't work...")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 13 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            mainViewModel.updatePermissionStatus()
        }
    }


}






