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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
            // Pas de FAB quand la liste est vide (l'état vide a déjà son bouton) ni en sélection.
            if (playlists.isNotEmpty() && !selectionMode) {
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
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    item {
                        Text(
                            text = "My Playlists",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp),
                        )
                    }
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

            if (selectionMode) {
                SelectionDeleteBar(
                    count = selectedIds.size,
                    onDelete = {
                        onDeletePlaylists(selectedIds)
                        exitSelection()
                    },
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
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
    val cardBrush = Brush.horizontalGradient(
        listOf(Aurora.Purple.copy(alpha = 0.12f), Aurora.Teal.copy(alpha = 0.12f)),
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(cardBrush)
            .then(if (selected) Modifier.background(Aurora.Purple.copy(alpha = 0.18f)) else Modifier)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Vignette colorée (dégradé accent) avec l'icône playlist.
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Aurora.AccentBrush),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_playlist),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(30.dp),
            )
        }
        Spacer(Modifier.size(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = playlist.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
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
            Spacer(Modifier.height(8.dp))
            // Pastille compteur de titres.
            Text(
                text = "${playlist.songTitles.size} titre(s)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = Aurora.Purple,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Aurora.Purple.copy(alpha = 0.14f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            )
        }
        if (selectionMode) {
            RadioButton(selected = selected, onClick = onClick)
        }
    }
}

@Composable
private fun PlaylistsEmptyState(modifier: Modifier = Modifier, onCreateClick: () -> Unit) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Icône playlist (Phosphor) multicolore : le dégradé est baké dans le vecteur.
        Image(
            painter = painterResource(R.drawable.ic_playlist_gradient),
            contentDescription = null,
            modifier = Modifier.size(104.dp),
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Aucune playlist",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Créez votre première playlist pour rassembler vos titres préférés.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        )
        Spacer(Modifier.height(28.dp))
        com.francotte.contentproviderformusic.ui.composable.GradientButton(
            text = "Créer une playlist",
            onClick = onCreateClick,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
    }
}
