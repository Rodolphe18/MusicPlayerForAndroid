package com.francotte.contentproviderformusic.ui.composable

import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import com.francotte.contentproviderformusic.model.Song
import com.francotte.contentproviderformusic.ui.theme.Aurora
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FloatingPlayerHost(
    modifier: Modifier = Modifier,
    songs: ImmutableList<Song>,
    currentSong: Song?,
    currentIndex: Int,
    isPlaying: Boolean,
    sliderValue: Float,
    onSongClick: (Int) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onPlayPause: () -> Unit,
    onSeek: (Float) -> Unit,
    onClose: () -> Unit,
    onToggleFavorite: (String, Boolean) -> Unit
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    // ---- TUNING UI ----
    val collapsedHeight = 70.dp
    val collapsedHorizontalPadding = 8.dp
    val expandedHorizontalPadding = 0.dp

    BoxWithConstraints(modifier.fillMaxSize()) {
        val fullHeightPx = with(density) { maxHeight.toPx() }
        if (fullHeightPx <= 1f) return@BoxWithConstraints

        // Insets bas (plus fiable que navigationBars sur certains devices)
        val bottomInsetDp = with(density) { WindowInsets.safeDrawing.getBottom(this).toDp() }

        val collapsedHeightPx = with(density) { collapsedHeight.toPx() }

        // ✅ Position Y du mini-player : on le remonte en laissant gapBottom sous la carte
        val collapsedY = fullHeightPx - collapsedHeightPx
        val expandedY = 0f

        val state = remember {
            AnchoredDraggableState(
                initialValue = PlayerSheetValue.Collapsed,
                positionalThreshold = { it * 0.35f },
                velocityThreshold = { with(density) { 120.dp.toPx() } },
                snapAnimationSpec = tween(durationMillis = 280),
                decayAnimationSpec = exponentialDecay()
            )
        }

        // Anchors
        SideEffect {
            state.updateAnchors(
                DraggableAnchors {
                    PlayerSheetValue.Collapsed at collapsedY
                    PlayerSheetValue.Expanded at expandedY
                }
            )
        }

        // Offset safe
        val yPx = (state.offset ?: collapsedY).let { if (it.isFinite()) it else collapsedY }

        // Progress 0..1 (safe)
        val denom = (collapsedY - expandedY).takeIf { it.isFinite() && it > 1f } ?: 1f
        val progress = (((collapsedY - yPx) / denom).takeIf { it.isFinite() } ?: 0f)
            .coerceIn(0f, 1f)

        // Animations visuelles
        val cornerDp = lerp(16.dp, 0.dp, progress).coerceAtLeast(0.dp)
        val sidePaddingDp = lerp(collapsedHorizontalPadding, expandedHorizontalPadding, progress)
            .coerceAtLeast(0.dp)

        // ---- LISTE ----
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 12.dp,
                end = 12.dp,
                top = 12.dp,
                bottom = collapsedHeight + bottomInsetDp + 12.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(songs, key = { _, s -> s.uri.toString() }) { index, song ->
                SongItem(song, song.uri == currentSong?.uri) {
                    onSongClick(index)
                    scope.launch { state.animateTo(PlayerSheetValue.Expanded) }
                }
            }
        }

        // ---- SCRIM ----
        if (progress > 0.02f) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f * progress))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        scope.launch { state.animateTo(PlayerSheetValue.Collapsed) }
                    }
            )
        }

        val sheetHeightDp =
            lerp(collapsedHeight, maxHeight, progress).coerceAtLeast(collapsedHeight)

        Box(
            Modifier
                .fillMaxWidth()
                .height(sheetHeightDp)
                .offset { IntOffset(0, yPx.toInt()) }
                .padding(horizontal = sidePaddingDp)
                .clip(RoundedCornerShape(cornerDp))
                .background(Aurora.BarBrush)
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(cornerDp))
                .anchoredDraggable(state = state, orientation = Orientation.Vertical)
        ) {
            if (currentSong == null) return@Box

            if (progress < 0.15f) {
                MiniPlayer(
                    song = currentSong,
                    isPlaying = isPlaying,
                    onPlayPause = onPlayPause,
                    onNext = onNext,
                    onPrevious = onPrevious,
                    onClose = onClose,
                    onExpand = { scope.launch { state.animateTo(PlayerSheetValue.Expanded) } }
                )
            } else {
                FullPlayer(
                    song = currentSong,
                    isPlaying = isPlaying,
                    sliderValue = sliderValue,
                    onPlayPause = onPlayPause,
                    onNext = onNext,
                    onPrevious = onPrevious,
                    onSeek = onSeek,
                    onToggleFavorite = onToggleFavorite)
            }
        }
    }
}