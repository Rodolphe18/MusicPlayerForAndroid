package com.example.contentproviderformusic

import android.Manifest
import android.content.ComponentName
import android.content.ContentUris
import android.content.Intent
import android.content.ServiceConnection
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
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.rememberImagePainter
import coil.decode.SvgDecoder
import coil.decode.VideoFrameDecoder
import com.example.contentproviderformusic.ui.theme.ContentProviderForMusicTheme
import java.util.concurrent.TimeUnit


class MainActivity : ComponentActivity(), ServiceConnection {

    var musicService: MainService? = null

    private val mainViewModel by viewModels<MainViewModel>()

    /** Handles playback of all the sound files  */
    private var mMediaPlayer: MediaPlayer? = null

    /** Handles audio focus when playing a sound file  */
    private var mAudioManager: AudioManager? = null

    private var length: Int? = null

    private val mOnAudioFocusChangeListener = AudioManager.OnAudioFocusChangeListener {
        fun onAudioFocusChange(focusChange: Int) {
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
                requestPermissions(
                    arrayOf(
                        Manifest.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK,
                        Manifest.permission.POST_NOTIFICATIONS,
                        Manifest.permission.READ_MEDIA_AUDIO,
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ), 0
                )
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
                    val songs = mutableListOf<Song>()
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
                            songs.add(Song(albumUri, data, title, artist, album, uri, duration))
                        }
                    }
                    mainViewModel.updateSongs(songs)
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LazyColumn(state = rememberLazyListState(), contentPadding = innerPadding) {
                        items(mainViewModel.songs) { song ->
                            Column {
                                Row(modifier = Modifier.clickable {
                                    Intent(
                                        applicationContext,
                                        MainService::class.java
                                    ).also {
                                        it.action = MainService.Actions.START.toString()
                                        it.putExtra("SONG_ID", song.uri.toString())
                                        startService(it)
                                    }
                                }, verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .weight(0.25f)
                                            .padding(
                                                start = 14.dp,
                                                end = 6.dp,
                                                top = 4.dp,
                                                bottom = 4.dp
                                            )
                                    ) {
                                        AsyncImage(error = rememberImagePainter(R.drawable.music_player_icon_slash_screen),
                                            placeholder = rememberImagePainter(R.drawable.music_player_icon_slash_screen),
                                            modifier = Modifier.size(50.dp),
                                            model = song.albumImage,
                                            contentDescription = null,
                                            imageLoader =
                                            ImageLoader.Builder(LocalContext.current).components {
                                                add(VideoFrameDecoder.Factory())
                                                add(SvgDecoder.Factory())
                                            }.build()
                                        )
                                    }
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(70.dp)
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = song.title,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Text(
                                        text = formatDuration(song.duration),
                                        modifier = Modifier.weight(0.3f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        if(musicService == null){
            val binder = service as MainService.MyBinder
            musicService = binder.currentService()
            musicService!!.mAudioManager = getSystemService(AUDIO_SERVICE) as AudioManager
            musicService!!.mAudioManager?.requestAudioFocus(musicService, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
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
            mAudioManager?.abandonAudioFocus(mOnAudioFocusChangeListener);

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
                mOnAudioFocusChangeListener,
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

    private val mCompletitionListener = OnCompletionListener { releaseMediaPlayer() }


}


data class Song(
    val albumImage: Uri,
    val data: String,
    val title: String,
    val artist: String,
    val album: String,
    val uri: Uri,
    val duration: Long = 0
)

fun formatDuration(duration: Long): String {
    val minutes = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
    val seconds = (TimeUnit.SECONDS.convert(duration, TimeUnit.MILLISECONDS) -
            minutes * TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES))
    return String.format("%02d:%02d", minutes, seconds)
}