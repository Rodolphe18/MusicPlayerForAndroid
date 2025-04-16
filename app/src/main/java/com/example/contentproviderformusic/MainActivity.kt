package com.example.contentproviderformusic

import android.Manifest
import android.R
import android.content.ContentUris
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import com.example.contentproviderformusic.ui.theme.ContentProviderForMusicTheme


class MainActivity : ComponentActivity() {

    private val mainViewModel by viewModels<MainViewModel>()

    /** Handles playback of all the sound files  */
    private var mMediaPlayer: MediaPlayer? = null

    /** Handles audio focus when playing a sound file  */
    private var mAudioManager: AudioManager? = null

    private var length:Int? = null

    private val mOnAudioFocusChangeListener = AudioManager.OnAudioFocusChangeListener {
       fun onAudioFocusChange(focusChange:Int) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                mMediaPlayer?.pause();
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                mMediaPlayer?.start();
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                length = mMediaPlayer?.currentPosition;
                Log.d("loss focus", "loss of focus");
                releaseMediaPlayer();
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ContentProviderForMusicTheme {
               requestPermissions(arrayOf(Manifest.permission.READ_MEDIA_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE), 0)
                val projection = arrayOf(MediaStore.Audio.Media._ID,MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST)
                contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, MediaStore.Audio.Media.IS_MUSIC + " != 0",null,null)?.use { cursor ->
                    val idColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
                    val dataColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                    val titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                    val artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                    val songs = mutableListOf<Song>()
                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idColumn)
                        val data = cursor.getString(dataColumn)
                        val title = cursor.getString(titleColumn)
                        val artist = cursor.getString(artistColumn)
                        val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)

                        songs.add(Song(data, title, artist, uri))
                        Log.d("debug_title",title)
                    }

                    mainViewModel.updateSongs(songs)
                }

                initMusicPlayer()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LazyColumn(state = rememberLazyListState(), contentPadding = innerPadding) {
                        items(mainViewModel.songs) { song ->
                            Column {
                              //  Text(song.data)
                                Text(song.title)
                                Text(song.artist)
                            }

                        }
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        releaseMediaPlayer()
    }


    private fun releaseMediaPlayer() {
        if (mMediaPlayer != null) {
            // Regardless of the current state of the media player, release its resources
            // because we no longer need it.
            mMediaPlayer?.also {
                it.release();
            }
            // Set the media player back to null. For our code, we've decided that
            // setting the media player to null is an easy way to tell that the media player
            // is not configured to play an audio file at the moment.
            mMediaPlayer = null;
            mAudioManager?.abandonAudioFocus(mOnAudioFocusChangeListener);

        }
    }

    private fun initMusicPlayer() {
        mAudioManager = getSystemService(AUDIO_SERVICE) as AudioManager


        /* Request audio focus so in order to play the audio file. The app needs to play a
    audio file, so we will request audio focus for unknown duration
   with AUDIOFOCUS_GAIN*/
        var result:Int? = 0
        result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //for API >= 26
            mAudioManager?.requestAudioFocus((AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)).build())
             } else {
            mAudioManager?.requestAudioFocus(
                mOnAudioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }


        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //create player
            mMediaPlayer = MediaPlayer.create(this, mainViewModel.songs[0].uri)
            //start playing
            Log.d("OnCreate method", "OnCreate player created")
            mMediaPlayer!!.start()
            //listen for completition of playing
            mMediaPlayer!!.setOnCompletionListener(mCompletitionListener)
        }
    }

    private val mCompletitionListener = OnCompletionListener { releaseMediaPlayer() }




}


data class Song(val data:String, val title:String, val artist:String, val uri: Uri)