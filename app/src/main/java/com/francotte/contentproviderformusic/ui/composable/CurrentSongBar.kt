package com.francotte.contentproviderformusic.ui.composable

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.francotte.contentproviderformusic.model.Song
import com.francotte.contentproviderformusic.ui.theme.Aurora


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CurrentSongBar(
    modifier: Modifier = Modifier,
    state: AnchoredDraggableState<DragAnchors>,
    song: Song,
    isPlaying: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onPlayPause: () -> Unit,
    sliderValue: Float,
    onSliderValueChanged: (Float) -> Unit,
    onClose: () -> Unit
) {
    val density = LocalDensity.current
    val extraHeight = with(density) { state.requireOffset().toDp() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height((150.dp + extraHeight).coerceIn(150.dp, 550.dp))
            .background(Aurora.BarBrush)
            .border(Dp.Hairline, Color.Black)
    ) {
        DraggableBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .anchoredDraggable(
                    state = state,
                    orientation = Orientation.Vertical,
                    enabled = true,
                    reverseDirection = true
                )
        )
        CurrentSongBarTitle(song = song, modifier = Modifier.padding(horizontal = 12.dp))
        Spacer(Modifier.height(8.dp))
        //UiControllerRow(isPlaying, onPrevious, onNext, onPlayPause, onClose)
        CustomSlider(song, Color.White, Color.White, Color.White, sliderValue, onSliderValueChanged)
    }
}

@Composable
fun DraggableBar(modifier: Modifier = Modifier) {
    Box(modifier) {
        Box(
            Modifier
                .align(Alignment.Center)
                .width(44.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.White.copy(alpha = 0.35f))
        )
    }
}