package com.example.contentproviderformusic

import androidx.compose.runtime.mutableStateListOf

object UserDataRepository {

    private val _songs = mutableStateListOf<Song>()
    val songs: List<Song> = _songs

    fun updateSongs(songs: List<Song>) {
        _songs.addAll(songs)
    }
}