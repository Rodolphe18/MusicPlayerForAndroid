package com.example.contentproviderformusic.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.ui.PlayerNotificationManager
import com.example.contentproviderformusic.R
import com.example.contentproviderformusic.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@UnstableApi
@AndroidEntryPoint
class MusicService : MediaSessionService() {

    @Inject
    lateinit var player: ExoPlayer

    private lateinit var session: MediaSession
//    private var pnManager: PlayerNotificationManager? = null

    private val channelId = "playback"
    private val notifId = 42

//    private val playerListener = object : Player.Listener {
//        override fun onEvents(player: Player, events: Player.Events) {
//            // Demande au PlayerNotificationManager de (re)poster la notif
//            pnManager?.invalidate()
//        }
//    }


    override fun onCreate() {
        super.onCreate()
        session = MediaSession.Builder(this, player).build()
        ensureChannel()
        setupNotification()
        registerReceiver(exitReceiver, IntentFilter(ACTION_EXIT), RECEIVER_NOT_EXPORTED)
     //   player.addListener(playerListener)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession = session

    override fun onDestroy() {
      //  player.removeListener(playerListener)
        unregisterReceiver(exitReceiver)
      //  pnManager?.setPlayer(null)
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

//        pnManager = PlayerNotificationManager.Builder(this, notifId, channelId)
//            .setMediaDescriptionAdapter(object : PlayerNotificationManager.MediaDescriptionAdapter {
//                override fun getCurrentContentTitle(player: Player): CharSequence =
//                    player.mediaMetadata.title ?: "Lecture"
//                override fun createCurrentContentIntent(player: Player): PendingIntent? = contentIntent
//                override fun getCurrentContentText(player: Player): CharSequence? =
//                    player.mediaMetadata.artist
//                override fun getCurrentLargeIcon(player: Player, cb: PlayerNotificationManager.BitmapCallback) = null
//            })
//            .setNotificationListener(object : PlayerNotificationManager.NotificationListener {
//                override fun onNotificationPosted(id: Int, notification: Notification, ongoing: Boolean) {
//                    // Quand ça joue -> notif "ongoing" + Foreground
//                    if (ongoing) {
//                        startForeground(id, notification)
//                    } else {
//                        // En pause : on garde la notif affichée mais on quitte le foreground
//                        stopForeground(false)
//                        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
//                        nm.notify(id, notification)
//                    }
//                }
//
//                override fun onNotificationCancelled(id: Int, dismissedByUser: Boolean) {
//                    // Si l’utilisateur balaye la notif -> on arrête le service
//                    stopForeground(true)
//                    stopSelf()
//                }
//            })
//            .setCustomActionReceiver(exitActionReceiver) // 👈 bouton EXIT
//            .build().apply {
//                setSmallIcon(R.drawable.ic_launcher_icon)
//                setUsePlayPauseActions(true)
//                setUseStopAction(true)    // bouton STOP (arrête le player)
//                setUsePreviousAction(false)
//                setUseNextAction(false)
//                setUseFastForwardAction(false)
//                setUseRewindAction(false)
//                setPlayer(player)         // brancher le player
//            }
    }

    private fun ensureChannel() {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val ch = NotificationChannel(channelId, "Lecture audio", NotificationManager.IMPORTANCE_LOW)
        nm.createNotificationChannel(ch)
    }

    // --- Action EXIT (custom) ---

    private val exitActionReceiver = object : PlayerNotificationManager.CustomActionReceiver {
        override fun createCustomActions(
            context: Context,
            instanceId: Int
        ): Map<String, NotificationCompat.Action> {
            val pi = PendingIntent.getBroadcast(
                context,
                0,
                Intent(ACTION_EXIT).setPackage(context.packageName),
                PendingIntent.FLAG_IMMUTABLE
            )
            val action = NotificationCompat.Action(
                R.drawable.ic_launcher_icon, // remplace par ton icône
                "Quitter",
                pi
            )
            return mapOf(ACTION_EXIT to action)
        }

        override fun getCustomActions(player: Player): MutableList<String> =
            mutableListOf(ACTION_EXIT)

        override fun onCustomAction(player: Player, action: String, intent: Intent) {
            if (action == ACTION_EXIT) {
                performExit()
            }
        }
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
      //  pnManager?.setPlayer(null)
        stopForeground(STOP_FOREGROUND_DETACH)
        stopSelf()
        // Optionnel : fermer l’activité en envoyant un broadcast que MainActivity écoute pour finish()
        sendBroadcast(Intent(ACTION_FINISH_ACTIVITIES).setPackage(packageName))
    }

    companion object {
        private const val ACTION_EXIT = "com.yourpkg.ACTION_EXIT"
        const val ACTION_FINISH_ACTIVITIES = "com.yourpkg.ACTION_FINISH_ACTIVITIES"
    }
}