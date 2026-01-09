package com.francotte.contentproviderformusic.model

import android.net.Uri
import androidx.media3.common.MediaItem

data class Song(
    val data: String,
    val title: String,
    val artist: String,
    val album: String,
    val uri: Uri,
    val duration: Long = 0,
    val mediaItem:MediaItem = MediaItem.fromUri(uri)
)