package com.example.contentproviderformusic

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

class MainService : Service() {

    private var mMediaPlayer: MediaPlayer? = null

    /** Handles audio focus when playing a sound file  */
    private var mAudioManager: AudioManager? = null

    private var length:Int? = null

    private val mCompletitionListener = MediaPlayer.OnCompletionListener { releaseMediaPlayer() }


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

    override fun onBind(intent: Intent?): IBinder? {
        TODO()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Actions.START.toString() -> {
                start()
                intent.getStringExtra("SONG_ID")?.let {
                    playSelectedSong(Uri.parse(it))
                }
            }
            Actions.PAUSE.toString() -> stopSelf()
            Actions.STOP.toString() -> stopSelf()
        }
        return super.onStartCommand(intent, flags, startId)
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


}