package com.example.contentproviderformusic

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MainViewModel:ViewModel() {

    companion object {
        private val _songs = mutableStateListOf<Song>()
        val songs:List<Song> = _songs

    }

    private val _permissionsGranted = MutableStateFlow(false)
    val permissionGranted = _permissionsGranted.asStateFlow()

    fun addSong(song: Song) {
        _songs.add(song)
    }

    fun updatePermissionStatus() {
        _permissionsGranted.value = true
    }

    fun updateSongs(songs:List<Song>) {
        _songs.addAll(songs)
    }
}