package com.francotte.contentproviderformusic.ui.playlists

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.francotte.contentproviderformusic.ui.MainViewModel
import com.francotte.contentproviderformusic.ui.composable.PLAYLISTS_ROUTE
import com.francotte.contentproviderformusic.ui.state.MusicAppState

const val PLAYLIST_CREATE_ROUTE = "playlist_create"
const val PLAYLIST_DETAIL_ROUTE = "playlist_detail"
const val PLAYLIST_ADD_SONGS_ROUTE = "playlist_add_songs"
const val PLAYLIST_ID_ARG = "playlistId"

fun NavController.navigateToPlayListsScreen(navOptions: NavOptions? = null) {
    this.navigate(PLAYLISTS_ROUTE, navOptions)
}

fun NavController.navigateToPlaylistCreate() = navigate(PLAYLIST_CREATE_ROUTE)
fun NavController.navigateToPlaylistDetail(id: Long) = navigate("$PLAYLIST_DETAIL_ROUTE/$id")
fun NavController.navigateToPlaylistAddSongs(id: Long) = navigate("$PLAYLIST_ADD_SONGS_ROUTE/$id")

fun NavGraphBuilder.playlistsGraph(
    appState: MusicAppState,
    mainViewModel: MainViewModel,
) {
    composable(route = PLAYLISTS_ROUTE) {
        val playlists by mainViewModel.playlists.collectAsStateWithLifecycle()
        PlaylistsScreen(
            appState = appState,
            playlists = playlists,
            onCreateClick = { appState.navController.navigateToPlaylistCreate() },
            onPlaylistClick = { id -> appState.navController.navigateToPlaylistDetail(id) },
            onDeletePlaylists = { ids -> mainViewModel.deletePlaylists(ids) },
        )
    }
}
