package com.francotte.contentproviderformusic.data

import kotlinx.coroutines.flow.Flow

interface UserDataRepository {

    val userData: Flow<UserData>

    suspend fun setFavoritesSongs(songTitle: String, isFavorite: Boolean)

    suspend fun createPlaylist(id: Long, title: String, description: String)

    suspend fun deletePlaylist(id: Long)

    suspend fun addSongToPlaylist(playlistId: Long, songTitle: String)

    suspend fun removeSongFromPlaylist(playlistId: Long, songTitle: String)
}
