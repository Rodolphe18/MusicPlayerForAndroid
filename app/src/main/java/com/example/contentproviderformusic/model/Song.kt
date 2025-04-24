package com.example.contentproviderformusic.model

import android.net.Uri

data class Song(
    val data: String,
    val title: String,
    val artist: String,
    val album: String,
    val uri: Uri,
    val duration: Long = 0
)