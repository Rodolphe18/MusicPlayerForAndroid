package com.francotte.contentproviderformusic.data

import kotlinx.coroutines.flow.Flow

interface UserDataRepository {


    val userData: Flow<UserData>

    suspend fun setFavoritesSongs(songTitle: String, isFavorite: Boolean)


}