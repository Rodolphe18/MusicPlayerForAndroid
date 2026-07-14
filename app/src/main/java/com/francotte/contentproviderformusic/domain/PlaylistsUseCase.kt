package com.francotte.contentproviderformusic.domain

import com.francotte.contentproviderformusic.data.UserDataRepository
import com.francotte.contentproviderformusic.model.Playlist
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PlaylistsUseCase @Inject constructor(userDataRepository: UserDataRepository) {

    val playlists: Flow<List<Playlist>> = userDataRepository.userData.map { it.playlists }
}
