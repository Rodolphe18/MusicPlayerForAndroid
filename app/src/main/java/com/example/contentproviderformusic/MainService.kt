package com.example.contentproviderformusic

import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.core.app.NotificationCompat

class MainService : Service(), AudioManager.OnAudioFocusChangeListener {

    var mMediaPlayer: MediaPlayer? = null

    private lateinit var mediaSession: MediaSessionCompat

    private var myBinder = MyBinder()

    var mAudioManager: AudioManager? = null

    private var length:Int? = null

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
        return START_STICKY
    }

    fun startCustomForegroundService(song: Song) {
        val imgArt = getImgArt(song.data)
        val image = if (imgArt != null) {
            BitmapFactory.decodeByteArray(imgArt, 0, imgArt.size)
        } else {
            BitmapFactory.decodeResource(resources, R.drawable.music_player_icon_slash_screen)
        }

        val notification = NotificationCompat
            .Builder(this, "main_channel")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Run is active")
            .setContentText("Run is active")
            .setContentTitle(song.title)
            .setContentText(song.artist)
            .setSmallIcon(R.drawable.music_player_icon_slash_screen).setLargeIcon(image)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSession.sessionToken))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .addAction(R.drawable.previous_icon, "Previous", null)
           // .addAction(playPauseBtn, "Play", playPendingIntent)
            .addAction(R.drawable.next_icon, "Next", null)
            .addAction(R.drawable.exit_icon, "Exit", null)
            .build()
        startForeground(13, notification)

    }

    fun releaseMediaPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer?.also { it.release(); }
            mMediaPlayer = null;
            mAudioManager?.abandonAudioFocus(this);

        }
    }




    override fun onAudioFocusChange(focusChange: Int) {
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