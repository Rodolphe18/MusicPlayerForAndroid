package com.francotte.contentproviderformusic.ui.playlists

import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.francotte.contentproviderformusic.domain.resolveByTitle
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

    composable(route = PLAYLIST_CREATE_ROUTE) {
        PlaylistCreateScreen(
            onBack = { appState.navController.popBackStack() },
            onCreate = { title, description ->
                val id = System.currentTimeMillis()
                mainViewModel.createPlaylist(id, title, description)
                appState.navController.navigate("$PLAYLIST_DETAIL_ROUTE/$id") {
                    popUpTo(PLAYLIST_CREATE_ROUTE) { inclusive = true }
                }
            },
        )
    }

    composable(
        route = "$PLAYLIST_DETAIL_ROUTE/{$PLAYLIST_ID_ARG}",
        arguments = listOf(navArgument(PLAYLIST_ID_ARG) { type = NavType.LongType }),
    ) { backStackEntry ->
        val playlistId = backStackEntry.arguments?.getLong(PLAYLIST_ID_ARG) ?: return@composable
        val playlists by mainViewModel.playlists.collectAsStateWithLifecycle()
        val allSongs by mainViewModel.songs.collectAsStateWithLifecycle()
        val currentSong by mainViewModel.currentPlayingSong.collectAsStateWithLifecycle()
        val playlist = playlists.find { it.id == playlistId }
        val playlistSongs = remember(playlist, allSongs) {
            playlist?.let { resolveByTitle(it.songTitles, allSongs) { s -> s.title } } ?: emptyList()
        }
        PlaylistDetailScreen(
            playlist = playlist,
            songs = playlistSongs,
            currentSong = currentSong,
            onBack = { appState.navController.popBackStack() },
            onAddSongsClick = { appState.navController.navigateToPlaylistAddSongs(playlistId) },
            onPlay = { list, index -> mainViewModel.playFromList(list, index) },
            onRemoveSong = { songTitle -> mainViewModel.removeSongFromPlaylist(playlistId, songTitle) },
        )
    }

    composable(
        route = "$PLAYLIST_ADD_SONGS_ROUTE/{$PLAYLIST_ID_ARG}",
        arguments = listOf(navArgument(PLAYLIST_ID_ARG) { type = NavType.LongType }),
    ) { backStackEntry ->
        val playlistId = backStackEntry.arguments?.getLong(PLAYLIST_ID_ARG) ?: return@composable
        val playlists by mainViewModel.playlists.collectAsStateWithLifecycle()
        val allSongs by mainViewModel.songs.collectAsStateWithLifecycle()
        val addedTitles = playlists.find { it.id == playlistId }?.songTitles ?: emptySet()
        PlaylistAddSongsScreen(
            songs = allSongs,
            addedTitles = addedTitles,
            onBack = { appState.navController.popBackStack() },
            onAdd = { songTitle -> mainViewModel.addSongToPlaylist(playlistId, songTitle) },
        )
    }
}
