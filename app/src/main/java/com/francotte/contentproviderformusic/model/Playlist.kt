package com.francotte.contentproviderformusic.model

data class Playlist(
    val id: Long,
    val title: String,
    val description: String,
    val songTitles: Set<String>,
)
