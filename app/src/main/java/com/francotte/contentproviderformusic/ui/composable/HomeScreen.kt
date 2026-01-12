package com.francotte.contentproviderformusic.ui.composable

import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.francotte.contentproviderformusic.model.Song

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    songs: List<Song>,
    currentIndex: Int,
    isPlaying: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onPlayPause: () -> Unit,
    sliderValue: Float,
    onSliderValueChanged: (Float) -> Unit,
    onClose: () -> Unit,
    onVerticalDrag: () -> Unit,
    onClick: (Int, Song) -> Unit
) {
    val listState = rememberLazyListState()
    val density = LocalDensity.current
    val state = remember {
        AnchoredDraggableState(
            initialValue = DragAnchors.Start,
            positionalThreshold = { it * 0.5f },
            velocityThreshold = { with(density) { 100.dp.toPx() } },
            snapAnimationSpec = tween(),
            decayAnimationSpec = exponentialDecay(),
            confirmValueChange = { target ->
                if (target == DragAnchors.End) onVerticalDrag()
                true
            }
        ).apply {
            updateAnchors(DraggableAnchors {
                DragAnchors.Start at 0f
                DragAnchors.End at 400f
            })
        }
    }
    Scaffold(modifier = modifier, topBar = {
        SongAppBar(Modifier, "Music Player", Icons.Filled.Search, Icons.Filled.Settings)
    }, bottomBar = {
        CurrentSongBar(
            modifier = Modifier.fillMaxWidth(),
            state = state,
            song = songs[currentIndex],
            isPlaying = isPlaying,
            onPrevious = onPrevious,
            onNext = onNext,
            onPlayPause = onPlayPause,
            sliderValue = sliderValue,
            onSliderValueChanged = onSliderValueChanged,
            onClose = onClose
        )
    }) { innerPadding ->
        LazyColumn(
            state = listState,
            contentPadding = innerPadding
        ) {
            itemsIndexed(songs) { index, song ->
                SongItem(song, index == currentIndex) { onClick(index, song) }
            }
        }
    }
}





