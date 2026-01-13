package com.francotte.contentproviderformusic.ui.navigation

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import com.francotte.contentproviderformusic.ui.MainViewModel
import com.francotte.contentproviderformusic.ui.composable.LIBRARY_ROUTE
import com.francotte.contentproviderformusic.ui.state.MusicAppState
import com.francotte.contentproviderformusic.ui.library.libraryScreen

@Composable
fun MusicNavHost(
    modifier: Modifier = Modifier,
    appState: MusicAppState,
    windowSizeClass: WindowSizeClass,
    mainViewModel: MainViewModel,
    isPlaying: Boolean,
    currentIndex: Int,
    currentDuration: Float,
) {
    val navController = appState.navController

    NavHost(
        navController = navController,
        startDestination = LIBRARY_ROUTE,
        modifier = modifier,
    ) {
        libraryScreen(
            windowSizeClass,
            appState,
            mainViewModel,
            isPlaying,
            currentIndex,
            currentDuration
        )

    }
}
