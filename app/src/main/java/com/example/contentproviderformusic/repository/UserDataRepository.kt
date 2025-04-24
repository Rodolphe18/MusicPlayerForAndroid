package com.example.contentproviderformusic.repository

import androidx.compose.runtime.mutableStateListOf
import com.example.contentproviderformusic.model.Song

object UserDataRepository {

    private val _songs = mutableStateListOf<Song>()
    val songs: List<Song> = _songs

    fun updateSongs(songs: List<Song>) {
        _songs.addAll(songs)
    }
}