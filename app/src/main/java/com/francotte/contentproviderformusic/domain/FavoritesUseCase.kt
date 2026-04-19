package com.francotte.contentproviderformusic.domain

import com.francotte.contentproviderformusic.data.UserDataRepository
import com.francotte.contentproviderformusic.model.Song
import com.francotte.contentproviderformusic.repository.SongsFetcherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class FavoritesUseCase @Inject constructor(userDataRepository: UserDataRepository) {

    val favoritesSongsTitles = userDataRepository.userData.map { it.favoritesSongs }

    val songs: Flow<List<Song>> = flow { SongsFetcherRepository.songs }

    operator fun invoke():Flow<List<Song>> = combine(favoritesSongsTitles, songs) { favs, songs ->
        songs.filter { it.title in favs }
    }

}