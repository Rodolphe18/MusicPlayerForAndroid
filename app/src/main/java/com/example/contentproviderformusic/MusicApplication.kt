package com.example.contentproviderformusic

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class MusicApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        if ( Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            val channel = NotificationChannel("main_channel", "notifications", NotificationManager.IMPORTANCE_HIGH)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object{
        const val CHANNEL_ID = "MusicNotification"
        const val PLAY = "play"
        const val NEXT = "next"
        const val PREVIOUS = "previous"
        const val EXIT = "exit"
    }

}