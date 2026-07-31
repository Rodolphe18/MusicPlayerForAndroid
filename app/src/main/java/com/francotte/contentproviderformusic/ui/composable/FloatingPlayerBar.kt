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
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import com.francotte.contentproviderformusic.model.Song
import com.francotte.contentproviderformusic.ui.theme.Aurora
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch

/**
 * Échafaudage du lecteur : rend [content] en fond (liste, détail…) puis, par-dessus,
 * le scrim + la carte "sheet" glissable qui bascule entre MiniPlayer et FullPlayer.
 *
 * [content] reçoit :
 *  - `bottomContentPadding` : l'espace à laisser en bas pour ne pas être masqué par le
 *    mini-player replié ;
 *  - `expand` : à appeler (ex. au clic d'une chanson) pour passer le lecteur en plein écran.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlayerSheetScaffold(
    currentSong: Song?,
    isPlaying: Boolean,
    sliderValue: Float,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onPlayPause: () -> Unit,
    onSeek: (Float) -> Unit,
    onClose: () -> Unit,
    onToggleFavorite: (String, Boolean) -> Unit,
    onAddToPlaylist: () -> Unit = {},
    isRepeatOneEnabled: Boolean = false,
    isShuffleEnabled: Boolean = false,
    onToggleRepeatOne: () -> Unit = {},
    onToggleShuffle: () -> Unit = {},
    onExpandedChange: (Boolean) -> Unit = {},
    // Décale le mini-player replié vers le haut (utile quand il n'y a pas de bottom bar en
    // dessous : il ne reste pas collé au bord bas de l'écran). N'affecte pas le plein écran.
    collapsedBottomInset: Dp = 0.dp,
    modifier: Modifier = Modifier,
    overlayContent: @Composable BoxScope.() -> Unit = {},
    content: @Composable (bottomContentPadding: Dp, expand: () -> Unit) -> Unit,
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

        val collapsedHeightPx = with(density) { collapsedHeight.toPx() }
        val collapsedBottomInsetPx = with(density) { collapsedBottomInset.toPx() }

        // Position repliée relevée de l'inset (mini player) ; position dépliée = 0 (plein écran).
        val collapsedY = fullHeightPx - collapsedHeightPx - collapsedBottomInsetPx
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

        SideEffect {
            state.updateAnchors(
                DraggableAnchors {
                    PlayerSheetValue.Collapsed at collapsedY
                    PlayerSheetValue.Expanded at expandedY
                }
            )
        }

        val yPx = (state.offset ?: collapsedY).let { if (it.isFinite()) it else collapsedY }

        val denom = (collapsedY - expandedY).takeIf { it.isFinite() && it > 1f } ?: 1f
        val progress = (((collapsedY - yPx) / denom).takeIf { it.isFinite() } ?: 0f)
            .coerceIn(0f, 1f)

        LaunchedEffect(progress > 0.15f) {
            onExpandedChange(progress > 0.15f)
        }

        val cornerDp = lerp(16.dp, 0.dp, progress).coerceAtLeast(0.dp)
        val sidePaddingDp = lerp(collapsedHorizontalPadding, expandedHorizontalPadding, progress)
            .coerceAtLeast(0.dp)

        val expand: () -> Unit = { scope.launch { state.animateTo(PlayerSheetValue.Expanded) } }

        // ---- CONTENU (fond) ----
        // On ne réserve l'espace du mini player QUE s'il y a une chanson courante ; sinon la
        // liste occupe toute la hauteur (pas de barre vide).
        content(if (currentSong != null) collapsedHeight + 12.dp + collapsedBottomInset else 0.dp, expand)

        // Éléments fixes de l'écran (par exemple la bottom bar), au-dessus du contenu mais
        // sous le player. La sheet les recouvre ainsi progressivement pendant son expansion.
        overlayContent()

        // Aucune chanson courante (ex. auto-play désactivé et aucun titre cliqué) : on n'affiche
        // ni le scrim ni la carte du player. La barre n'apparaît qu'au 1er clic sur un titre.
        val song = currentSong ?: return@BoxWithConstraints

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
            if (progress < 0.15f) {
                MiniPlayer(
                    song = song,
                    isPlaying = isPlaying,
                    onPlayPause = onPlayPause,
                    onNext = onNext,
                    onPrevious = onPrevious,
                    onClose = onClose,
                    onExpand = expand
                )
            } else {
                FullPlayer(
                    song = song,
                    isPlaying = isPlaying,
                    sliderValue = sliderValue,
                    onPlayPause = onPlayPause,
                    onNext = onNext,
                    onPrevious = onPrevious,
                    onSeek = onSeek,
                    onToggleFavorite = onToggleFavorite,
                    isRepeatOneEnabled = isRepeatOneEnabled,
                    isShuffleEnabled = isShuffleEnabled,
                    onToggleRepeatOne = onToggleRepeatOne,
                    onToggleShuffle = onToggleShuffle,
                    onAddToPlaylist = onAddToPlaylist
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FloatingPlayerHost(
    modifier: Modifier = Modifier,
    collapsedBottomInset: Dp = 0.dp,
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
    onToggleFavorite: (String, Boolean) -> Unit,
    onAddToPlaylist: () -> Unit = {},
    isRepeatOneEnabled: Boolean = false,
    isShuffleEnabled: Boolean = false,
    onToggleRepeatOne: () -> Unit = {},
    onToggleShuffle: () -> Unit = {},
    onExpandedChange: (Boolean) -> Unit = {},
    overlayContent: @Composable BoxScope.() -> Unit = {},
    header: (@Composable () -> Unit)? = null,
    emptyContent: @Composable () -> Unit = {}
) {
    PlayerSheetScaffold(
        modifier = modifier,
        currentSong = currentSong,
        isPlaying = isPlaying,
        sliderValue = sliderValue,
        onPrevious = onPrevious,
        onNext = onNext,
        onPlayPause = onPlayPause,
        onSeek = onSeek,
        onClose = onClose,
        onToggleFavorite = onToggleFavorite,
        isRepeatOneEnabled = isRepeatOneEnabled,
        isShuffleEnabled = isShuffleEnabled,
        onToggleRepeatOne = onToggleRepeatOne,
        onToggleShuffle = onToggleShuffle,
        onExpandedChange = onExpandedChange,
        onAddToPlaylist = onAddToPlaylist,
        collapsedBottomInset = collapsedBottomInset,
        overlayContent = overlayContent,
    ) { bottomContentPadding, expand ->
        // ---- EMPTY STATE ---- (derrière la liste ; la carte player reste au-dessus)
        if (songs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                emptyContent()
            }
        }

        // ---- LISTE ----
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 12.dp,
                end = 12.dp,
                top = 12.dp,
                bottom = bottomContentPadding
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // En-tête (titre + actions) : premier item de la liste, donc défile au scroll.
            if (header != null) {
                item(key = "header") { header() }
            }
            itemsIndexed(songs, key = { index, s -> "${s.uri}#$index" }) { index, song ->
                SongItem(song, song.uri == currentSong?.uri, isPlaying) {
                    onSongClick(index)
                    expand()
                }
            }
        }
    }
}
