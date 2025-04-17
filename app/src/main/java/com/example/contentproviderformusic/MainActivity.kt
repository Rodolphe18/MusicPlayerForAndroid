package com.example.contentproviderformusic

import android.Manifest
import android.content.ComponentName
import android.content.ContentUris
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.app.ActivityCompat
import com.example.contentproviderformusic.ui.theme.ContentProviderForMusicTheme


class MainActivity : ComponentActivity(), ServiceConnection {

    var musicService: MainService? = null

    private val mainViewModel by viewModels<MainViewModel>()

    /** Handles playback of all the sound files  */
    private var mMediaPlayer: MediaPlayer? = null

    /** Handles audio focus when playing a sound file  */
    private var mAudioManager: AudioManager? = null

    private var length: Int? = null


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ContentProviderForMusicTheme {
                val permissionGranted by mainViewModel.permissionGranted.collectAsState(false)
                requestRuntimePermission()
                getUserSongs()
                if (permissionGranted || requestRuntimePermission()) {
                    MainScreen(MainViewModel.songs) { song ->
                        initServiceAndPlaylist()
                    }
                }
            }
        }
    }

    private fun requestRuntimePermission() :Boolean{
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU){
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE,)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 13)
                return false
            }
        }else{
            //android 13 or Higher permission request
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_AUDIO,Manifest.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK,
                    Manifest.permission.POST_NOTIFICATIONS), 13)
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
            if(requestCode == 13 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission Granted",Toast.LENGTH_SHORT).show()
                mainViewModel.updatePermissionStatus()
            }
    }


    private fun getUserSongs() {
        val projection = arrayOf(
            MediaStore.Audio.Media.ALBUM_ID,
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
            val idAlbumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
            val dataColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            val titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
            val durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val albumId = cursor.getLong(idAlbumColumn).toString()
                val data = cursor.getString(dataColumn)
                val title = cursor.getString(titleColumn)
                val artist = cursor.getString(artistColumn)
                val album = cursor.getString(albumColumn)
                val duration = cursor.getLong(durationColumn)
                val uri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                val artUri = Uri.parse("content://media/external/audio/albumart")
                val albumUri = Uri.withAppendedPath(artUri, albumId)
                //   Log.d("debug_album", albumUri)
                if (duration > 0) {
                    mainViewModel.addSong(Song(albumUri, data, title, artist, album, uri, duration))
                }
            }
        }
    }


    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        if (musicService == null) {
            val binder = service as MainService.MyBinder
            musicService = binder.currentService()
            musicService!!.mAudioManager = getSystemService(AUDIO_SERVICE) as AudioManager
            musicService!!.mAudioManager?.requestAudioFocus(
                musicService,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
        //  createMediaPlayer()
        //  musicService!!.seekBarSetup()


    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null
    }

    override fun onStop() {
        super.onStop()
        releaseMediaPlayer()
    }


    private fun releaseMediaPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer?.also { it.release(); }
            mMediaPlayer = null;
        }
    }

    private fun playSelectedSong(uri: Uri) {
        releaseMediaPlayer()
        mAudioManager = getSystemService(AUDIO_SERVICE) as AudioManager

        var result: Int? = 0
        result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //for API >= 26
            mAudioManager?.requestAudioFocus((AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)).build())
        } else {
            mAudioManager?.requestAudioFocus(
                musicService,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }


        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //create player
            mMediaPlayer = MediaPlayer.create(this, uri)
            //start playing
            Log.d("OnCreate method", "OnCreate player created")
            mMediaPlayer!!.start()
            //listen for completition of playing
            mMediaPlayer!!.setOnCompletionListener(mCompletitionListener)
        }
    }

    private fun initServiceAndPlaylist() {
        val intent = Intent(this, MainService::class.java)
        bindService(intent, this, BIND_AUTO_CREATE)
        startService(intent)
    }

    private val mCompletitionListener = OnCompletionListener { releaseMediaPlayer() }


}




