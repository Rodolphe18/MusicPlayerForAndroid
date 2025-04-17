package com.example.contentproviderformusic

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.Image
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Icon
import androidx.compose.ui.semantics.Role.Companion.Image
import androidx.core.app.NotificationCompat

class MainService : Service(), AudioManager.OnAudioFocusChangeListener {

    private var mMediaPlayer: MediaPlayer? = null

    private var myBinder = MyBinder()

    /** Handles audio focus when playing a sound file  */
    var mAudioManager: AudioManager? = null

    private var length:Int? = null

    private val mCompletitionListener = MediaPlayer.OnCompletionListener { releaseMediaPlayer() }


    private val mOnAudioFocusChangeListener = AudioManager.OnAudioFocusChangeListener {
        fun onAudioFocusChange(focusChange:Int) {

        }
    }

    override fun onBind(intent: Intent?): IBinder {
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

    private fun start() {
        val notification = NotificationCompat
            .Builder(this, "main_channel")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Run is active")
            .setContentText("Run is active")
            .build()
        startForeground(1, notification)

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


    enum class Actions {
        START, PAUSE, STOP
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