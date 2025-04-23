package com.example.contentproviderformusic

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.ContentUris
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.app.ActivityCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.contentproviderformusic.ui.theme.ContentProviderForMusicTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

class MainActivity : ComponentActivity(), ServiceConnection {

    private var musicService: MainService? = null

    private val mainViewModel by viewModels<MainViewModel>()

    private val isPlaying = MutableStateFlow(false)

    private val currentSong: MutableStateFlow<Song?> = MutableStateFlow(null)

    private val currentDuration: MutableStateFlow<Float?> = MutableStateFlow(0f)


    @SuppressLint("StateFlowValueCalledInComposition")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ContentProviderForMusicTheme {
                val permissionGranted by mainViewModel.permissionGranted.collectAsState(false)
                val isPlaying by isPlaying.collectAsStateWithLifecycle()
                val currentDuration by currentDuration.collectAsStateWithLifecycle()
                val currentSong by currentSong.collectAsStateWithLifecycle()
                requestRuntimePermission()
                getUserSongs()
                initServiceAndPlaylist()
                if (permissionGranted || requestRuntimePermission()) {
                    if (currentSong == null) {
                        MainScreen(MainViewModel.songs) { index, song ->
                            musicService?.playSelectedSong(song)
                            musicService?.indexSong?.update { AtomicInteger(index) }
                            musicService?.startCustomForegroundService(song, R.drawable.pause_icon)
                        }

                    } else if (intent.getStringExtra("open_song") != null) {

                        currentSong?.let { song ->
                            SongScreen(song = song,
                                isPlaying = isPlaying,
                                onPrevious = { musicService?.prevSong() },
                                onNext = { musicService?.nextSong() },
                                onPlayPause = { if (isPlaying) musicService?.pauseMusic() else musicService?.playMusic() },
                                sliderValue = currentDuration!!,
                                onSliderValueChanged = { musicService?.onSeekBarValueChanged(it) })
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
                                onSliderValueChanged = { musicService?.onSeekBarValueChanged(it) })
                        }
                    }
                }
            }
        }
    }

    private fun initServiceAndPlaylist() {
        val intent = Intent(this, MainService::class.java)
        startService(intent)
        bindService(intent, this, BIND_AUTO_CREATE)
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
                isPlaying.value = it
            }
        }
        lifecycleScope.launch {
            (service as MainService.MyBinder).currentDuration().collectLatest {
                currentDuration.value = it
            }
        }
        lifecycleScope.launch {
            (service as MainService.MyBinder).getCurrentSong().collectLatest {
                currentSong.value = it
            }
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null
    }


    private fun requestRuntimePermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    13
                )
                return false
            }
        } else {
            //android 13 or Higher permission request
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        Manifest.permission.READ_MEDIA_AUDIO,
                        Manifest.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK,
                        Manifest.permission.POST_NOTIFICATIONS
                    ), 13
                )
                return false
            }
        }
        return true
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


    private fun getUserSongs() {
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
        )

        contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            MediaStore.Audio.Media.IS_MUSIC + " != 0",
            null,
            null
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val dataColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            val titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
            val durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val data = cursor.getString(dataColumn)
                val title = cursor.getString(titleColumn)
                val artist = cursor.getString(artistColumn)
                val album = cursor.getString(albumColumn)
                val duration = cursor.getLong(durationColumn)
                val uri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                if (duration > 0) {
                    mainViewModel.addSong(Song(data, title, artist, album, uri, duration))
                }
            }
        }
    }


}





