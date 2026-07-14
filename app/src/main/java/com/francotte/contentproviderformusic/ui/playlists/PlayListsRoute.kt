package com.francotte.contentproviderformusic.ui.playlists

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        // Placeholder — remplacé en Task 8.
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Playlists")
        }
    }
}
