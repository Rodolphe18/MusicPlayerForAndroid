package com.example.contentproviderformusic.service

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.contentproviderformusic.R
import com.example.contentproviderformusic.model.Song
import com.example.contentproviderformusic.ui.MainActivity
import com.example.contentproviderformusic.ui.ScreenStatus
import com.example.contentproviderformusic.utils.MediaManager
import com.example.contentproviderformusic.utils.getImgArt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.exitProcess

class MainService : Service(), AudioManager.OnAudioFocusChangeListener, Player.Listener {

    lateinit var player: Player

    private lateinit var mediaSession: MediaSessionCompat

    private var myBinder = MyBinder()

    var mAudioManager: AudioManager? = null

    val isPlaying = MutableStateFlow(false)

    val currentIndex = MutableStateFlow(AtomicInteger(0))

    val currentDuration: MutableStateFlow<Float?> = MutableStateFlow(0f)

    private val scope = CoroutineScope(Dispatchers.Main)

    private var job: Job? = null

    val screenStatus = MutableStateFlow(ScreenStatus.MAIN_SCREEN)

    inner class MyBinder : Binder() {
        fun currentService(): MainService {
            return this@MainService
        }

        fun isPlaying() = this@MainService.isPlaying
        fun getCurrentIndex() = this@MainService.currentIndex
        fun currentDuration() = this@MainService.currentDuration
        fun getScreenStatus() = this@MainService.screenStatus
    }

    override fun onBind(intent: Intent?): IBinder {
        mediaSession = MediaSessionCompat(baseContext, "My Music")
        player = ExoPlayer.Builder(this).build()
        player.setMediaItems(MediaManager.getUserSongs(this).map { it.mediaItem }, 0, 0)
        player.addListener(this)
        player.prepare()
        player.play()
        updateSongDuration()
        startCustomForegroundService(
            MediaManager.getUserSongs(this)[player.currentMediaItemIndex],
            R.drawable.pause_icon
        )
        return myBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            PREVIOUS -> prevSong()
            PLAY -> if (player.isPlaying) pauseMusic() else playMusic()
            NEXT -> nextSong()
            EXIT -> exitApplication()
        }
        return START_STICKY
    }

    private fun updateSongDuration() {
        job?.cancel()
        job = scope.launch {
            while (true) {
                currentDuration.update { player.currentPosition.toFloat() }
                delay(1000)
            }
        }
    }

    fun onSeekBarValueChanged(value: Float) {
        job?.cancel()
        currentDuration.update { value }
        player.seekTo(value.toLong())

        job = scope.launch {
            while (true) {
                currentDuration.update { player.currentPosition.toFloat() }
                delay(1000)
            }
        }
    }


    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        super.onMediaItemTransition(mediaItem, reason)
        currentIndex.update { AtomicInteger(player.currentMediaItemIndex) }
        startCustomForegroundService(MediaManager.getUserSongs(this)[player.currentMediaItemIndex])
        updateSongDuration()
    }

    fun playSelectedSong(index:Int) {
        isPlaying.update { true }
        player.seekTo(index, 0)
        player.prepare()
        player.play()
        updateSongDuration()
        startCustomForegroundService(
            MediaManager.getUserSongs(this)[index],
            R.drawable.pause_icon
        )
    }

    fun playMusic() {
        isPlaying.update { true }
        player.prepare()
        player.play()
        updateSongDuration()
        startCustomForegroundService(
            MediaManager.getUserSongs(this)[player.currentMediaItemIndex],
            R.drawable.pause_icon
        )
    }

    fun pauseMusic() {
        isPlaying.update { false }
        player.prepare()
        player.pause()
        updateSongDuration()
        startCustomForegroundService(
            MediaManager.getUserSongs(this)[player.currentMediaItemIndex],
            R.drawable.play_icon
        )
    }

    fun stopSong() {
        job?.cancel()
        player.stop()
    }

    fun prevSong() {
        player.seekToPrevious()
        currentIndex.update { AtomicInteger(player.currentMediaItemIndex) }
        updateSongDuration()
        startCustomForegroundService(MediaManager.getUserSongs(this)[player.currentMediaItemIndex])
    }

    fun nextSong() {
        player.seekToNext()
        currentIndex.update { AtomicInteger(player.currentMediaItemIndex) }
        updateSongDuration()
        startCustomForegroundService(MediaManager.getUserSongs(this)[player.currentMediaItemIndex])
    }

    private fun startCustomForegroundService(song: Song, playPauseBtn: Int = R.drawable.pause_icon) {

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
            mediaSession.setMetadata(
                MediaMetadataCompat.Builder().putLong(
                    MediaMetadataCompat.METADATA_KEY_DURATION, player.duration
                ).build()
            )
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
                    job?.cancel()
                    currentDuration.update { pos.toFloat() }
                    player.seekTo(pos)

                    job = scope.launch {
                        while (true) {
                            currentDuration.update { pos.toFloat() }
                            delay(1000)
                        }
                    }
                    mediaSession.setPlaybackState(getPlayBackState())
                }
            })
        }
        startForeground(13, notification)
    }


    fun getPlayBackState(): PlaybackStateCompat {
        val playbackSpeed = if (player.isPlaying) 1F else 0F

        return PlaybackStateCompat.Builder()
            .setState(
                if (player.isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
                player.currentPosition, playbackSpeed
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


    @SuppressLint("NewApi")
    fun exitApplication() {
        mAudioManager?.abandonAudioFocusRequest((AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)).build())
        stopForeground(STOP_FOREGROUND_REMOVE)
        player.release()
        exitProcess(1)
    }


    companion object {
        const val PLAY = "play"
        const val NEXT = "next"
        const val PREVIOUS = "previous"
        const val EXIT = "exit"
    }


}