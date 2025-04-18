package com.example.contentproviderformusic

import android.app.Service
import android.media.MediaMetadataRetriever
import com.example.contentproviderformusic.MainActivity.Companion.musicService
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

fun getImgArt(path: String): ByteArray? {
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(path)
    return retriever.embeddedPicture
}

fun formatDuration(duration: Long): String {
    val minutes = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
    val seconds = (TimeUnit.SECONDS.convert(duration, TimeUnit.MILLISECONDS) -
            minutes * TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES))
    return String.format(Locale.FRANCE,"%02d:%02d", minutes, seconds)
}

fun exitApplication() {
    musicService?.let { service ->
        service.mAudioManager?.abandonAudioFocus(service)
        service.stopForeground(Service.STOP_FOREGROUND_REMOVE)
        service.mMediaPlayer?.release()
    }
    musicService = null
    exitProcess(1)
}