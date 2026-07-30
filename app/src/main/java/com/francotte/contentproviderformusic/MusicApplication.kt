package com.francotte.contentproviderformusic

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MusicApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            val channel = NotificationChannel("main_channel", "notifications", NotificationManager.IMPORTANCE_HIGH)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Initialisation du SDK Google Mobile Ads (opération I/O -> hors du thread principal).
        // Le consentement (UMP) est géré séparément dans ConsentManager au moment de l'affichage.
        Thread { MobileAds.initialize(this) }.start()
    }

}