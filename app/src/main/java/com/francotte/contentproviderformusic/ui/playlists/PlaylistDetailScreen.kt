package com.francotte.contentproviderformusic.ui.playlists

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.francotte.contentproviderformusic.R
import com.francotte.contentproviderformusic.model.Playlist
import com.francotte.contentproviderformusic.model.Song
import com.francotte.contentproviderformusic.ui.composable.PlayerSheetScaffold
import com.francotte.contentproviderformusic.ui.theme.Aurora

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playlist: Playlist?,
    accent: Color,
    songs: List<Song>,
    currentSong: Song?,
    isPlaying: Boolean,
    sliderValue: Float,
    onBack: () -> Unit,
    onAddSongsClick: () -> Unit,
    onPlay: (List<Song>, Int) -> Unit,
    onRemoveSong: (String) -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Float) -> Unit,
    onClose: () -> Unit,
    onToggleFavorite: (String, Boolean) -> Unit,
    onAddToPlaylist: () -> Unit = {},
    isRepeatOneEnabled: Boolean = false,
    onToggleRepeatOne: () -> Unit = {},
) {
    var selectionMode by remember { mutableStateOf(false) }
    var selectedTitles by remember { mutableStateOf(emptySet<String>()) }

    fun exitSelection() {
        selectionMode = false
        selectedTitles = emptySet()
    }

    fun toggle(title: String) {
        val next = if (title in selectedTitles) selectedTitles - title else selectedTitles + title
        if (next.isEmpty()) exitSelection() else selectedTitles = next
    }

    BackHandler(enabled = selectionMode) { exitSelection() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Aurora.CoralBackground,
        topBar = {
            TopAppBar(
                title = { Text(playlist?.title ?: "") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Aurora.CoralBackground,
                    scrolledContainerColor = Aurora.CoralBackground,
                    titleContentColor = Aurora.Night,
                    navigationIconContentColor = Aurora.Night,
                    actionIconContentColor = Aurora.Night,
                ),
                navigationIcon = {
                    IconButton(onClick = { if (selectionMode) exitSelection() else onBack() }) {
                        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = stringResource(R.string.cd_back))
                    }
                },
            )
        },
        bottomBar = {
            if (selectionMode) {
                SelectionDeleteBar(
                    count = selectedTitles.size,
                    onDelete = {
                        selectedTitles.forEach { onRemoveSong(it) }
                        exitSelection()
                    },
                    modifier = Modifier.navigationBarsPadding(),
                )
            }
        },
    ) { innerPadding ->
        PlayerSheetScaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(Aurora.CoralBackground)
                .padding(innerPadding),
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
            onToggleRepeatOne = onToggleRepeatOne,
            onAddToPlaylist = onAddToPlaylist,
            // Pas de bottom bar ici : on relève le mini player pour qu'il ne colle pas au bord bas.
            collapsedBottomInset = 20.dp,
        ) { bottomContentPadding, expand ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = bottomContentPadding + 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // En-tête : poster typographique avec le titre répété.
                item {
                    PlaylistTitleCard(
                        title = playlist?.title.orEmpty(),
                        color = accent,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RectangleShape,
                    )
                }

                // Rangée "ajouter des titres" : bouton circulaire glass + texte.
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                            .clickable(enabled = !selectionMode) { onAddSongsClick() }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(accent),
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_add),
                                contentDescription = stringResource(R.string.add_songs),
                                tint = Color.White,
                                modifier = Modifier.align(Alignment.Center),
                            )
                        }
                        Spacer(Modifier.size(12.dp))
                        Text(stringResource(R.string.add_songs), style = MaterialTheme.typography.titleMedium)
                    }
                }

                // Titres : clic = lecture + plein écran ; appui long = mode suppression.
                itemsIndexed(songs, key = { index, song -> "${song.uri}#$index" }) { index, song ->
                    PlaylistSongRow(
                        song = song,
                        modifier = Modifier.padding(horizontal = 12.dp),
                        isCurrent = song.uri == currentSong?.uri,
                        isPlaying = isPlaying,
                        selected = song.title in selectedTitles,
                        onClick = {
                            if (selectionMode) {
                                toggle(song.title)
                            } else {
                                onPlay(songs, index)
                                expand()
                            }
                        },
                        onLongClick = {
                            if (!selectionMode) {
                                selectionMode = true
                                selectedTitles = setOf(song.title)
                            }
                        },
                        trailing = {
                            if (selectionMode) {
                                RadioButton(
                                    selected = song.title in selectedTitles,
                                    onClick = { toggle(song.title) },
                                )
                            }
                        },
                    )
                }
            }
        }
    }
}
