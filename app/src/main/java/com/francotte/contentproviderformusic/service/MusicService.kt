package com.francotte.contentproviderformusic.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.francotte.contentproviderformusic.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@UnstableApi
@AndroidEntryPoint
class MusicService : MediaSessionService() {

    @Inject
    lateinit var player: ExoPlayer

    private lateinit var session: MediaSession

    private val channelId = "playback"


    val bluetoothManager: BluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
    
    val d = bluetoothManager.adapter

        val s = d.bluetoothLeScanner
    
    val bluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)

        }
    }



    override fun onCreate() {
        super.onCreate()
        session = MediaSession.Builder(this, player).build()
        ensureChannel()
        setupNotification()
        registerReceiver(exitReceiver, IntentFilter(ACTION_EXIT), RECEIVER_NOT_EXPORTED)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession = session

    override fun onDestroy() {
        unregisterReceiver(exitReceiver)
        session.release()
        player.release()
        super.onDestroy()
    }

    // --- Notification ---

    private fun setupNotification() {
        val contentIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }


    private fun ensureChannel() {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val ch = NotificationChannel(channelId, "Lecture audio", NotificationManager.IMPORTANCE_LOW)
        nm.createNotificationChannel(ch)
    }

    fun dd(): ByteArray {
       return "kdkdk".encodeToByteArray()
    }

    private val exitReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_EXIT) performExit()
        }
    }

    private fun performExit() {
        // Stop lecture + notif + service
        player.clearMediaItems()
        player.playWhenReady = false
        stopForeground(STOP_FOREGROUND_DETACH)
        stopSelf()
        // Optionnel : fermer l’activité en envoyant un broadcast que MainActivity écoute pour finish()
        sendBroadcast(Intent(ACTION_FINISH_ACTIVITIES).setPackage(packageName))
    }

    companion object {
        private const val ACTION_EXIT = "com.francotte.contentproviderformusic.ACTION_EXIT"
        const val ACTION_FINISH_ACTIVITIES = "com.yourpkg.contentproviderformusic.ACTION_FINISH_ACTIVITIES"
    }
}