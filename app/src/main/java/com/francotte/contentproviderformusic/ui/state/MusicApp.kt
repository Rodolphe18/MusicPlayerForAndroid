package com.francotte.contentproviderformusic.ui.state

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.francotte.contentproviderformusic.ui.MainViewModel
import com.francotte.contentproviderformusic.ui.composable.FAVORITES_ROUTE
import com.francotte.contentproviderformusic.ui.composable.LIBRARY_ROUTE
import com.francotte.contentproviderformusic.ui.composable.PLAYLISTS_ROUTE
import com.francotte.contentproviderformusic.ui.composable.TopLevelDestination
import com.francotte.contentproviderformusic.ui.favorites.navigateToFavoritesScreen
import com.francotte.contentproviderformusic.ui.library.navigateToLibraryScreen
import com.francotte.contentproviderformusic.ui.navigation.MusicNavHost
import com.francotte.contentproviderformusic.ui.playlists.navigateToPlayListsScreen

@Composable
fun MusicApp(mainViewModel: MainViewModel, windowSizeClass: WindowSizeClass) {
    val isPlaying by mainViewModel.isPlaying.collectAsStateWithLifecycle()
    val currentIndex by mainViewModel.currentIndex.collectAsStateWithLifecycle()
    val currentDuration by mainViewModel.currentDuration.collectAsStateWithLifecycle()

    val appState = rememberMusicAppState()

    MusicNavHost(
        appState = appState,
        windowSizeClass = windowSizeClass,
        mainViewModel = mainViewModel,
        isPlaying = isPlaying,
        currentIndex = currentIndex,
        currentDuration = currentDuration,
    )
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

    val topLevelDestinations: List<TopLevelDestination>
        @Composable get() {
            return listOf(
                TopLevelDestination.LIBRARY,
                TopLevelDestination.FAVORITES,
                TopLevelDestination.PLAYLISTS
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