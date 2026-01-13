package com.francotte.contentproviderformusic.ui.favorites

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import com.francotte.contentproviderformusic.repository.UserDataRepository
import com.francotte.contentproviderformusic.ui.MainViewModel
import com.francotte.contentproviderformusic.ui.composable.FAVORITES_ROUTE
import com.francotte.contentproviderformusic.ui.composable.HomeScreen
import com.francotte.contentproviderformusic.ui.composable.LIBRARY_ROUTE
import com.francotte.contentproviderformusic.ui.state.MusicAppState

fun NavController.navigateToFavoritesScreen(navOptions: NavOptions? = null) {
    this.navigate(FAVORITES_ROUTE, navOptions)
}

fun NavGraphBuilder.favoritesScreen(
    windowSizeClass: WindowSizeClass,
    musicAppState: MusicAppState,
    mainViewModel: MainViewModel,
    isPlaying: Boolean,
    currentIndex: Int,
    currentDuration: Float
) {
    composable(route = FAVORITES_ROUTE) {
        FavoritesRoute(
            mainViewModel = mainViewModel,
            windowSizeClass = windowSizeClass,
            musicAppState = musicAppState,
            isPlaying = isPlaying,
            currentIndex = currentIndex,
            currentDuration = currentDuration
        )
    }
}

@Composable
fun FavoritesRoute(
    mainViewModel: MainViewModel = hiltViewModel(),
    windowSizeClass: WindowSizeClass,
    musicAppState: MusicAppState,
    isPlaying: Boolean,
    currentIndex: Int,
    currentDuration: Float
) {


    Box(Modifier.fillMaxSize()) {
        HomeScreen(
            modifier = Modifier.fillMaxSize(),
            windowSizeClass = windowSizeClass,
            appState = musicAppState,
            songs = emptyList(),
            currentIndex = currentIndex,
            isPlaying = isPlaying,
            onPrevious = { mainViewModel.prevSong() },
            onNext = { mainViewModel.nextSong() },
            onPlayPause = { mainViewModel.playPause() },
            sliderValue = currentDuration,
            onSliderValueChanged = {
                mainViewModel.onSeekBarValueChanged(
                    it
                )
            },
            onClose = { mainViewModel.stopSong() },

            onClick = { index ->
                mainViewModel.playSelectedSong(index)
            })
    }


}
