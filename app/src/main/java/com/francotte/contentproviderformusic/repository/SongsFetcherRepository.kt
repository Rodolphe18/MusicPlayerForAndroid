package com.francotte.contentproviderformusic.repository

import androidx.compose.runtime.mutableStateListOf
import com.francotte.contentproviderformusic.model.Song

object SongsFetcherRepository {

    private val _songs = mutableStateListOf<Song>()
    val songs: List<Song> = _songs

    // Remplace le contenu : cet object est un singleton de processus, et l'appelant
    // (LaunchedEffect sur permissionGranted) se relance à chaque recréation d'activité.
    // Un simple addAll dupliquait toute la bibliothèque à chaque rotation.
    fun updateSongs(songs: List<Song>) {
        _songs.clear()
        _songs.addAll(songs)
    }
}