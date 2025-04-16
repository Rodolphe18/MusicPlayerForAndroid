package com.example.contentproviderformusic

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

class MainViewModel:ViewModel() {

    val songs = mutableStateListOf<Song>()

    fun updateSongs(songs:List<Song>) {
        this.songs.addAll(songs)
    }
}