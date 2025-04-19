package com.example.contentproviderformusic

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.exitProcess

class MainService : Service(), AudioManager.OnAudioFocusChangeListener, OnCompletionListener {

    var mMediaPlayer: MediaPlayer? = null

    private lateinit var mediaSession: MediaSessionCompat

    private var myBinder = MyBinder()

    var mAudioManager: AudioManager? = null

    private var length:Int? = null

    var isPlaying = false

    var currentSong: Song? = null
    var indexSong: AtomicInteger = AtomicInteger(-1)

    override fun onBind(intent: Intent?): IBinder {
        mediaSession = MediaSessionCompat(baseContext, "My Music")
        return myBinder
    }

    inner class MyBinder : Binder() {
        fun currentService(): MainService {
            return this@MainService
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action){
            PREVIOUS -> prevSong(this)
            PLAY -> if(mMediaPlayer?.isPlaying == true) pauseMusic() else playMusic()
            NEXT -> if(MainViewModel.songs.size > 1)  nextSong(this)
            EXIT -> exitApplication()
        }
        return START_STICKY
    }

    fun playSelectedSong(uri: Uri) {
        isPlaying = true
        mMediaPlayer?.release()
        mAudioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        var result: Int? = 0
        result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mAudioManager?.requestAudioFocus((AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)).build())
        } else {
            mAudioManager?.requestAudioFocus(
                this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mMediaPlayer = MediaPlayer.create(this, uri)
            mMediaPlayer?.start()
            mMediaPlayer?.setOnCompletionListener(this)
        }
    }

    private fun playMusic(){
        isPlaying = true
        mMediaPlayer?.start()
        startCustomForegroundService(currentSong!!,R.drawable.pause_icon)
     }

    private fun pauseMusic(){
        isPlaying = false
        mMediaPlayer?.pause()
        startCustomForegroundService(currentSong!!, R.drawable.play_icon)
    }

    private fun prevSong(context: Context){
        if(MainViewModel.songs.size > 1) {
            mMediaPlayer?.release()
            currentSong = null
            indexSong.getAndDecrement()
            if (indexSong.get() > -1 && indexSong.get() < MainViewModel.songs.size - 1) {
                currentSong = MainViewModel.songs[indexSong.get()]
                currentSong?.let { song ->
                    mMediaPlayer = MediaPlayer.create(context, song.uri)
                    mMediaPlayer?.start()
                    //  playSelectedSong(song.uri)
                    startCustomForegroundService(song)
                }
            }
        }
    }

    private fun nextSong(context: Context) {
        if (MainViewModel.songs.size > 1) {
            mMediaPlayer?.release()
            currentSong = null
            indexSong.getAndIncrement()
            if (indexSong.get() > -1 && indexSong.get() < MainViewModel.songs.size - 1) {
                currentSong = MainViewModel.songs[indexSong.get()]
                currentSong?.let { song ->
                    mMediaPlayer = MediaPlayer.create(context, song.uri)
                    mMediaPlayer?.start()
                    startCustomForegroundService(song)
                }
            }
        }
    }

    fun startCustomForegroundService(song: Song,playPauseBtn: Int=R.drawable.pause_icon) {

        val intent = Intent(baseContext, MainActivity::class.java)

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
        val image:Bitmap? = if (imgArt != null) {
            BitmapFactory.decodeByteArray(imgArt, 0, imgArt.size)
        } else {
            BitmapFactory.decodeResource(resources, R.drawable.music_player_icon_slash_screen)
        }

        val notification = NotificationCompat
            .Builder(this, "main_channel")
            .setContentTitle(song.title)
            .setContentText(song.artist)
            .setSmallIcon(R.drawable.music_player_icon_slash_screen).setLargeIcon(image)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSession.sessionToken))
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
                        MediaMetadataCompat.METADATA_KEY_DURATION,it.toLong()
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
            mMediaPlayer!!.currentPosition.toLong(), playbackSpeed)
            .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE or PlaybackStateCompat.ACTION_SEEK_TO or PlaybackStateCompat.ACTION_SKIP_TO_NEXT or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
            .build()
    }

    fun handlePlayPause() {
      //  if (PlayerActivity.isPlaying) pauseMusic() else playMusic()

        //update playback state for notification
        mediaSession.setPlaybackState(getPlayBackState())
    }

    override fun onAudioFocusChange(focusChange: Int) {
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
            mMediaPlayer?.pause();
        } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            mMediaPlayer?.start();
        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            length = mMediaPlayer?.currentPosition;
            Log.d("loss focus", "loss of focus");
            mMediaPlayer?.release()
        }
    }

    override fun onCompletion(mp: MediaPlayer?) {
        currentSong = null
        indexSong.getAndIncrement()
        if (indexSong.get() > -1 && indexSong.get() < MainViewModel.songs.size) {
            currentSong = MainViewModel.songs[indexSong.get()]
            currentSong?.let { song ->
                playSelectedSong(song.uri)
                startCustomForegroundService(song)
            }
        } else {
            indexSong = AtomicInteger(0)
            currentSong = MainViewModel.songs[indexSong.get()]
            currentSong?.let { song ->
                playSelectedSong(song.uri)
                startCustomForegroundService(song)
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

    companion object{
        const val PLAY = "play"
        const val NEXT = "next"
        const val PREVIOUS = "previous"
        const val EXIT = "exit"
    }


}