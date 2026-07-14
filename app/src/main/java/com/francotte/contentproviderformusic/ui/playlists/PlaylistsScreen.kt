package com.francotte.contentproviderformusic.ui.playlists

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.francotte.contentproviderformusic.R
import com.francotte.contentproviderformusic.model.Playlist
import com.francotte.contentproviderformusic.ui.composable.BottomBar
import com.francotte.contentproviderformusic.ui.state.MusicAppState
import com.francotte.contentproviderformusic.ui.theme.Aurora

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistsScreen(
    appState: MusicAppState,
    playlists: List<Playlist>,
    onCreateClick: () -> Unit,
    onPlaylistClick: (Long) -> Unit,
    onDeletePlaylists: (Set<Long>) -> Unit,
) {
    var selectionMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(emptySet<Long>()) }

    fun exitSelection() {
        selectionMode = false
        selectedIds = emptySet()
    }

    fun toggle(id: Long) {
        val next = if (id in selectedIds) selectedIds - id else selectedIds + id
        if (next.isEmpty()) exitSelection() else selectedIds = next
    }

    // Ferme le mode suppression au bouton retour système.
    BackHandler(enabled = selectionMode) { exitSelection() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomBar(
                modifier = Modifier.fillMaxWidth(),
                destinations = appState.topLevelDestinations,
                onNavigateToDestination = appState::navigateToTopLevelDestination,
                currentDestination = appState.currentDestination,
            )
        },
        floatingActionButton = {
            if (!selectionMode) {
                FloatingActionButton(
                    onClick = onCreateClick,
                    shape = RoundedCornerShape(16.dp),
                    containerColor = Aurora.Purple,
                    contentColor = Color.White,
                    modifier = Modifier.padding(16.dp),
                ) {
                    Icon(painterResource(R.drawable.ic_add), contentDescription = "Créer une playlist")
                }
            }
        },
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            if (playlists.isEmpty()) {
                PlaylistsEmptyState(
                    modifier = Modifier.fillMaxSize(),
                    onCreateClick = onCreateClick,
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(playlists, key = { it.id }) { playlist ->
                        PlaylistCard(
                            playlist = playlist,
                            selectionMode = selectionMode,
                            selected = playlist.id in selectedIds,
                            onClick = {
                                if (selectionMode) toggle(playlist.id)
                                else onPlaylistClick(playlist.id)
                            },
                            onLongClick = {
                                if (!selectionMode) {
                                    selectionMode = true
                                    selectedIds = setOf(playlist.id)
                                }
                            },
                        )
                    }
                }
            }

            // Barre du bas persistante avec l'icône poubelle (mode suppression).
            if (selectionMode) {
                Surface(
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
                    color = Aurora.Purple,
                    shadowElevation = 8.dp,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("${selectedIds.size} sélectionnée(s)", color = Color.White)
                        IconButton(onClick = {
                            onDeletePlaylists(selectedIds)
                            exitSelection()
                        }) {
                            Icon(
                                painterResource(R.drawable.ic_delete),
                                contentDescription = "Supprimer",
                                tint = Color.White,
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PlaylistCard(
    playlist: Playlist,
    selectionMode: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Aurora.Purple.copy(alpha = if (selected) 0.16f else 0.08f))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = playlist.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (playlist.description.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = playlist.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${playlist.songTitles.size} titre(s)",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            )
        }
        if (selectionMode) {
            RadioButton(selected = selected, onClick = onClick)
        }
    }
}

@Composable
private fun PlaylistsEmptyState(modifier: Modifier = Modifier, onCreateClick: () -> Unit) {
    val gradient = Brush.linearGradient(listOf(Aurora.Purple, Aurora.Teal, Aurora.Cyan))
    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Icône teintée par un dégradé (SrcAtop sur les pixels opaques) → multicolore.
        Image(
            painter = painterResource(R.drawable.playlist_icon),
            contentDescription = null,
            modifier = Modifier
                .size(96.dp)
                .drawWithContent {
                    drawContent()
                    drawRect(brush = gradient, blendMode = BlendMode.SrcAtop)
                },
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = "Aucune playlist",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Créez votre première playlist pour rassembler vos titres préférés.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onCreateClick) {
            Text("Créer une playlist")
        }
    }
}
