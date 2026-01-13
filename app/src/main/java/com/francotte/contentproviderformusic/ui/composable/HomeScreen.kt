package com.francotte.contentproviderformusic.ui.composable

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.francotte.contentproviderformusic.model.Song
import com.francotte.contentproviderformusic.ui.state.MusicAppState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    windowSizeClass: WindowSizeClass,
    appState: MusicAppState,
    songs: List<Song>,
    currentIndex: Int,
    isPlaying: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onPlayPause: () -> Unit,
    sliderValue: Float,
    onSliderValueChanged: (Float) -> Unit,
    onClose: () -> Unit,
    onClick: (Int) -> Unit
) {
    Scaffold(modifier = modifier, topBar = {
        SongAppBar(Modifier, "Music Player", Icons.Filled.Search, Icons.Filled.Settings)
    }, bottomBar = {
        BottomBar(
            modifier = Modifier.fillMaxWidth(),
            destinations = appState.topLevelDestinations,
            onNavigateToDestination = appState::navigateToTopLevelDestination,
            currentDestination = appState.currentDestination
        )

    }) { innerPadding ->
        FloatingPlayerHost(
            modifier = Modifier.padding(top = innerPadding.calculateTopPadding(), bottom = innerPadding.calculateBottomPadding()),
            songs = songs,
            currentIndex = currentIndex,
            isPlaying = isPlaying,
            onPrevious = onPrevious,
            onNext = onNext,
            onSongClick = onClick,
            onPlayPause = onPlayPause,
            sliderValue = sliderValue,
            onSeek = onSliderValueChanged,
            onClose = onClose
        )
    }
}





