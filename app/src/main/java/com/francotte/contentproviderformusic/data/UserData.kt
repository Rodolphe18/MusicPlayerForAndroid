package com.francotte.contentproviderformusic.data

import com.francotte.contentproviderformusic.model.Playlist

data class UserData(
    val favoritesSongs: Set<String>,
    val playlists: List<Playlist>,
)
