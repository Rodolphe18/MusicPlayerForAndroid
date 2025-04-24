package com.example.contentproviderformusic.service

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.contentproviderformusic.ui.MainActivity
import com.example.contentproviderformusic.R
import com.example.contentproviderformusic.ui.ScreenStatus
import com.example.contentproviderformusic.repository.UserDataRepository
import com.example.contentproviderformusic.utils.getImgArt
import com.example.contentproviderformusic.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.exitProcess

class MainService : Service(), AudioManager.OnAudioFocusChangeListener, OnCompletionListener {

    var mMediaPlayer: MediaPlayer? = null

    private lateinit var mediaSession: MediaSessionCompat

    private var myBinder = MyBinder()

    var mAudioManager: AudioManager? = null

    val isPlaying = MutableStateFlow(false)

    val currentSong: MutableStateFlow<Song?> = MutableStateFlow(null)

    val indexSong = MutableStateFlow(AtomicInteger(-1))

    val currentDuration: MutableStateFlow<Float?> = MutableStateFlow(0f)

    private val scope = CoroutineScope(Dispatchers.Main)

    private var job: Job? = null

    val screenStatus = MutableStateFlow(ScreenStatus.MAIN_SCREEN)

    inner class MyBinder : Binder() {
        fun currentService(): MainService { return this@MainService }
        fun isPlaying() = this@MainService.isPlaying
        fun getCurrentSong() = this@MainService.currentSong
        fun currentDuration() = this@MainService.currentDuration
        fun getScreenStatus() = this@MainService.screenStatus
    }

    override fun onBind(intent: Intent?): IBinder {
        mediaSession = MediaSessionCompat(baseContext, "My Music")
        return myBinder
    }

   private fun updateSongDuration() {
       job?.cancel()
        job = scope.launch {
            if (mMediaPlayer?.isPlaying?.not() == true) return@launch
            while (true) {
                currentDuration.update { mMediaPlayer?.currentPosition?.toFloat() }
                delay(1000)
            }
        }
    }

    fun stopSong() {
        job?.cancel()
        currentSong.value = null
        mMediaPlayer?.release()

    }

    fun onSeekBarValueChanged(value:Float) {
        job?.cancel()
        currentDuration.update { value }
        mMediaPlayer?.seekTo(value.toInt())
        job = scope.launch {
            if (mMediaPlayer?.isPlaying?.not() == true) return@launch
            while (true) {
                currentDuration.update { mMediaPlayer?.currentPosition?.toFloat() }
                delay(1000)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            PREVIOUS -> prevSong()
            PLAY -> if (mMediaPlayer?.isPlaying == true) pauseMusic() else playMusic()
            NEXT -> nextSong()
            EXIT -> exitApplication()
        }
        return START_STICKY
    }

    fun playSelectedSong(song: Song) {
        job?.cancel()
        isPlaying.update { true }
        mMediaPlayer?.release()
        mMediaPlayer = MediaPlayer.create(this,song.uri)
        mMediaPlayer?.start()
        mMediaPlayer?.setOnCompletionListener(this)
        currentSong.value = song
        updateSongDuration()
    }

    fun playMusic() {
        job?.cancel()
        isPlaying.update { true }
        mMediaPlayer?.start()
        updateSongDuration()
        startCustomForegroundService(currentSong.value!!, R.drawable.pause_icon)
    }

    fun pauseMusic() {
        job?.cancel()
        isPlaying.update { false }
        mMediaPlayer?.pause()
        updateSongDuration()
        startCustomForegroundService(currentSong.value!!, R.drawable.play_icon)
    }

    fun prevSong() {
        job?.cancel()
        if (UserDataRepository.songs.size > 1) {
            mMediaPlayer?.release()
            currentSong.value = null
            indexSong.value.getAndDecrement()
            if (indexSong.value.get() > -1 && indexSong.value.get() < UserDataRepository.songs.size - 1) {
                currentSong.update { UserDataRepository.songs[indexSong.value.get()] }
                currentSong.value?.let { song ->
                    mMediaPlayer = MediaPlayer.create(this, song.uri)
                    mMediaPlayer?.start()
                    startCustomForegroundService(song)
                    updateSongDuration()
                }
            } else {
                indexSong.update { AtomicInteger(UserDataRepository.songs.size - 1) }
                currentSong.update { UserDataRepository.songs[indexSong.value.get()] }
                currentSong.value?.let { song ->
                    mMediaPlayer = MediaPlayer.create(this, song.uri)
                    mMediaPlayer?.start()
                    startCustomForegroundService(song)
                    updateSongDuration()
                }
            }
        }
    }

    fun nextSong() {
        job?.cancel()
        if (UserDataRepository.songs.size > 1) {
            mMediaPlayer?.release()
            currentSong.value = null
            indexSong.value.getAndIncrement()
            if (indexSong.value.get() > -1 && indexSong.value.get() < UserDataRepository.songs.size) {
                currentSong.update { UserDataRepository.songs[indexSong.value.get()] }
                currentSong.value?.let { song ->
                    mMediaPlayer = MediaPlayer.create(this, song.uri)
                    mMediaPlayer?.start()
                    startCustomForegroundService(song)
                    updateSongDuration()
                }
            } else {
                indexSong.update { AtomicInteger(0) }
                currentSong.update { UserDataRepository.songs[indexSong.value.get()] }
                currentSong.value?.let { song ->
                    mMediaPlayer = MediaPlayer.create(this, song.uri)
                    mMediaPlayer?.start()
                    startCustomForegroundService(song)
                    updateSongDuration()
                }
            }
        }
    }

    fun startCustomForegroundService(song: Song, playPauseBtn: Int = R.drawable.pause_icon) {

        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("open_song", "")
            setAction("act")
        }

        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val contentIntent = PendingIntent.getActivity(this, 0, intent, flag)

        val prevIntent = Intent(this, MainService::class.java).setAction(PREVIOUS)
        val prevPendingIntent = PendingIntent.getService(this, 0, prevIntent, flag)

        val playIntent = Intent(this, MainService::class.java).setAction(PLAY)
        val playPendingIntent = PendingIntent.getService(this, 0, playIntent, flag)

        val nextIntent = Intent(this, MainService::class.java).setAction(NEXT)
        val nextPendingIntent = PendingIntent.getService(this, 0, nextIntent, flag)

        val exitIntent = Intent(this, MainService::class.java).setAction(EXIT)
        val exitPendingIntent = PendingIntent.getService(this, 0, exitIntent, flag)

        val imgArt = getImgArt(song.data)
        val image: Bitmap? = if (imgArt != null) {
            BitmapFactory.decodeByteArray(imgArt, 0, imgArt.size)
        } else {
            BitmapFactory.decodeResource(resources, R.drawable.music_player_icon_slash_screen2)
        }

        val notification = NotificationCompat
            .Builder(this, "main_channel")
            .setContentIntent(contentIntent)
            .setContentTitle(song.title)
            .setContentText(song.artist)
            .setSmallIcon(R.drawable.music_player_icon_slash_screen2).setLargeIcon(image)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .addAction(R.drawable.previous_icon, "Previous", prevPendingIntent)
            .addAction(playPauseBtn, "Play", playPendingIntent)
            .addAction(R.drawable.next_icon, "Next", nextPendingIntent)
            .addAction(R.drawable.exit_icon, "Exit", exitPendingIntent)
            .build()


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mMediaPlayer?.duration?.let {
                mediaSession.setMetadata(
                    MediaMetadataCompat.Builder().putLong(
                        MediaMetadataCompat.METADATA_KEY_DURATION, it.toLong()
                    ).build()
                )
            }
            mediaSession.setPlaybackState(getPlayBackState())
            mediaSession.setCallback(object : MediaSessionCompat.Callback() {

                //called when headphones buttons are pressed
                //currently only pause or play music on button click
                override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
                    return super.onMediaButtonEvent(mediaButtonEvent)
                }

                //called when seekbar is changed
                override fun onSeekTo(pos: Long) {
                    super.onSeekTo(pos)
                    mMediaPlayer?.seekTo(pos.toInt())
                    mediaSession.setPlaybackState(getPlayBackState())
                }
            })
        }
        startForeground(13, notification)
    }


    fun getPlayBackState(): PlaybackStateCompat {
        val playbackSpeed = if (mMediaPlayer?.isPlaying == true) 1F else 0F

        return PlaybackStateCompat.Builder()
            .setState(
                if (mMediaPlayer?.isPlaying == true) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
                mMediaPlayer!!.currentPosition.toLong(), playbackSpeed
            )
            .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
            .build()
    }


    override fun onAudioFocusChange(focusChange: Int) {
        if (focusChange <= 0) {
            pauseMusic()
        } else {
            playMusic()
        }
    }

    override fun onCompletion(mp: MediaPlayer?) {
        job?.cancel()
        Log.d("debug_on_completion", "completion")
        currentSong.value = null
        indexSong.value.getAndIncrement()
        if (indexSong.value.get() > -1 && indexSong.value.get() < UserDataRepository.songs.size) {
            currentSong.update { UserDataRepository.songs[indexSong.value.get()] }
            currentSong.value?.let { song ->
                playSelectedSong(song)
                startCustomForegroundService(song)
                updateSongDuration()
            }
        } else {
            indexSong.update { AtomicInteger(0) }
            currentSong.update { UserDataRepository.songs[indexSong.value.get()] }
            currentSong.value?.let { song ->
                playSelectedSong(song)
                startCustomForegroundService(song)
                updateSongDuration()
            }
        }
    }

    @SuppressLint("NewApi")
    fun exitApplication() {
        mAudioManager?.abandonAudioFocusRequest((AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)).build())
        stopForeground(STOP_FOREGROUND_REMOVE)
        mMediaPlayer?.release()
        exitProcess(1)
    }

    companion object {
        const val PLAY = "play"
        const val NEXT = "next"
        const val PREVIOUS = "previous"
        const val EXIT = "exit"
    }


}