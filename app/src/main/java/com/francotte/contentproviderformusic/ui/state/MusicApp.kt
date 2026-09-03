package com.francotte.contentproviderformusic.ui.state

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.francotte.contentproviderformusic.ui.MainViewModel
import com.francotte.contentproviderformusic.ads.Banner
import com.francotte.contentproviderformusic.ui.composable.FAVORITES_ROUTE
import com.francotte.contentproviderformusic.ui.composable.LIBRARY_ROUTE
import com.francotte.contentproviderformusic.ui.composable.PLAYLISTS_ROUTE
import com.francotte.contentproviderformusic.ui.composable.TopLevelDestination
import com.francotte.contentproviderformusic.ui.favorites.navigateToFavoritesScreen
import com.francotte.contentproviderformusic.ui.library.navigateToLibraryScreen
import com.francotte.contentproviderformusic.ui.navigation.MusicNavHost
import com.francotte.contentproviderformusic.ui.playlists.AddToPlaylistOverlay
import com.francotte.contentproviderformusic.ui.playlists.navigateToPlayListsScreen
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
fun MusicApp(mainViewModel: MainViewModel, windowSizeClass: WindowSizeClass) {
    val isPlaying by mainViewModel.isPlaying.collectAsStateWithLifecycle()
    val currentIndex by mainViewModel.currentIndex.collectAsStateWithLifecycle()
    val currentDuration by mainViewModel.currentDuration.collectAsStateWithLifecycle()

    val appState = rememberMusicAppState()

    val addToPlaylistSong by mainViewModel.addToPlaylistSong.collectAsStateWithLifecycle()
    val playlists by mainViewModel.playlists.collectAsStateWithLifecycle()

    // Expose les testTag comme resource-id : sans cela UiAutomator ne voit aucun
    // noeud Compose (generation du Baseline Profile).
    Column(
        Modifier
            .fillMaxSize()
            .semantics { testTagsAsResourceId = true },
    ) {
        Box(Modifier.weight(1f)) {
            MusicNavHost(
                appState = appState,
                windowSizeClass = windowSizeClass,
                mainViewModel = mainViewModel,
                isPlaying = isPlaying,
                currentIndex = currentIndex,
                currentDuration = currentDuration,
            )

            // Overlay "Ajouter à" partagé : piloté par le VM, affiché au-dessus du NavHost
            // pour couvrir aussi le player, quel que soit l'écran d'origine du clic sur "+".
            addToPlaylistSong?.let { song ->
                AddToPlaylistOverlay(
                    song = song,
                    playlists = playlists,
                    onToggle = { playlist ->
                        if (song.title in playlist.songTitles) {
                            mainViewModel.removeSongFromPlaylist(playlist.id, song.title)
                        } else {
                            mainViewModel.addSongToPlaylist(playlist.id, song.title)
                        }
                    },
                    onCreatePlaylist = { title, description ->
                        mainViewModel.createPlaylist(System.currentTimeMillis(), title, description)
                    },
                    onClose = { mainViewModel.closeAddToPlaylist() },
                )
            }
        }

        // La bannière n'est composée qu'une fois le CMP résolu et les annonces autorisées :
        // la composer plus tôt déclencherait une requête publicitaire avant le consentement.
        val canShowAds by mainViewModel.canShowAds.collectAsStateWithLifecycle()
        if (canShowAds) {
            val adRequest = remember(canShowAds) { mainViewModel.buildAdRequest() }
            Banner(
                useAdaptiveSize = false,
                horizontalPadding = 0.dp,
                heightFallback = 50.dp,
                adRequest = adRequest,
            )
        }
    }
}

@Composable
fun rememberMusicAppState(
    navController: NavHostController = rememberNavController(),
): MusicAppState {
    return remember(navController) {
        MusicAppState(navController)
    }
}

@Stable
class MusicAppState(val navController: NavHostController) {

    private val previousDestination = mutableStateOf<NavDestination?>(null)

    val currentDestination: NavDestination?
        @Composable get() {
            val currentEntry =
                navController.currentBackStackEntryFlow.collectAsState(initial = null)
            return currentEntry.value?.destination.also { destination ->
                if (destination != null) {
                    previousDestination.value = destination
                }
            } ?: previousDestination.value
        }

    val topLevelDestinations: ImmutableList<TopLevelDestination>
        @Composable get() {
            return persistentListOf(
                TopLevelDestination.LIBRARY,
                TopLevelDestination.FAVORITES,
                TopLevelDestination.PLAYLISTS,
            )
        }


    val currentTopLevelDestination: TopLevelDestination?
        @Composable get() {
            val destination = currentDestination ?: return null
            val currentRoute = destination.route

            return when (currentRoute) {
                LIBRARY_ROUTE -> TopLevelDestination.LIBRARY
                FAVORITES_ROUTE -> TopLevelDestination.FAVORITES
                PLAYLISTS_ROUTE -> TopLevelDestination.PLAYLISTS
                else -> null
            }
        }


    fun navigateToTopLevelDestination(topLevelDestination: TopLevelDestination) {
        val topLevelNavOptions = navOptions {
            popUpTo(navController.graph.findStartDestination().id)
        }

        when (topLevelDestination) {
            is TopLevelDestination.LIBRARY -> navController.navigateToLibraryScreen(topLevelNavOptions)
            is TopLevelDestination.FAVORITES -> navController.navigateToFavoritesScreen(topLevelNavOptions)
            is TopLevelDestination.PLAYLISTS -> navController.navigateToPlayListsScreen(topLevelNavOptions)
        }
    }


}
