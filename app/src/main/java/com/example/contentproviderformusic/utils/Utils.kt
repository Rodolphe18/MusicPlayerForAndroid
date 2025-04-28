package com.example.contentproviderformusic.utils

import android.media.MediaMetadataRetriever
import java.util.Locale
import java.util.concurrent.TimeUnit

fun getImgArt(path: String): ByteArray? {
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(path)
    return retriever.embeddedPicture
}

fun formatDuration(duration: Long): String {
    val hour = TimeUnit.HOURS.convert(duration, TimeUnit.MILLISECONDS)
    val minutes = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS) -
            hour * TimeUnit.MINUTES.convert(1, TimeUnit.HOURS)
    val seconds = (TimeUnit.SECONDS.convert(duration, TimeUnit.MILLISECONDS) -
            minutes * TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES))-
            hour * TimeUnit.SECONDS.convert(1, TimeUnit.HOURS)
    return if (hour > 0) String.format(Locale.FRANCE,"%2d:%02d:%02d", hour, minutes, seconds) else
        String.format(Locale.FRANCE,"%02d:%02d", minutes, seconds)
}

fun formatDuration(duration: Float): String {
    val hour = TimeUnit.HOURS.convert(duration.toLong(), TimeUnit.MILLISECONDS)
    val minutes = TimeUnit.MINUTES.convert(duration.toLong(), TimeUnit.MILLISECONDS) -
            hour * TimeUnit.MINUTES.convert(1, TimeUnit.HOURS)
    val seconds = (TimeUnit.SECONDS.convert(duration.toLong(), TimeUnit.MILLISECONDS) -
            minutes * TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES)) -
            hour * TimeUnit.MINUTES.convert(1, TimeUnit.HOURS)
    return if (hour > 0) String.format(Locale.FRANCE,"%2d:%02d:%2d", hour, minutes, seconds) else
        String.format(Locale.FRANCE,"%02d:%02d", minutes, seconds)}
