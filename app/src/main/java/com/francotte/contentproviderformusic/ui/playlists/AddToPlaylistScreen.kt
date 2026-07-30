package com.francotte.contentproviderformusic.ui.playlists

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.francotte.contentproviderformusic.R
import com.francotte.contentproviderformusic.model.Playlist
import com.francotte.contentproviderformusic.model.Song
import com.francotte.contentproviderformusic.ui.theme.Aurora

/**
 * Écran plein écran "Ajouter à" lancé depuis le player : choisir une playlist où ajouter
 * la chanson, ou en créer une nouvelle (formulaire inline). Le "+" d'une playlist devient
 * une pastille de validation corail quand la chanson y figure.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToPlaylistOverlay(
    song: Song,
    playlists: List<Playlist>,
    onToggle: (Playlist) -> Unit,
    onCreatePlaylist: (title: String, description: String) -> Unit,
    onClose: () -> Unit,
) {
    var showCreate by rememberSaveable { mutableStateOf(false) }

    // Retour système : depuis la création -> liste ; depuis la liste -> ferme l'écran.
    BackHandler(enabled = showCreate) { showCreate = false }
    BackHandler(enabled = !showCreate) { onClose() }

    // Fond opaque : couvre entièrement le player en dessous et intercepte les taps.
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (showCreate) {
            PlaylistCreateScreen(
                onBack = { showCreate = false },
                onCreate = { title, description ->
                    onCreatePlaylist(title, description)
                    showCreate = false
                },
            )
        } else {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                topBar = {
                    TopAppBar(
                        title = { Text(stringResource(R.string.add_to_playlist_title)) },
                        navigationIcon = {
                            IconButton(onClick = onClose) {
                                Icon(
                                    painterResource(R.drawable.ic_arrow_back),
                                    contentDescription = stringResource(R.string.cd_back),
                                )
                            }
                        },
                    )
                },
            ) { padding ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(vertical = 8.dp),
                ) {
                    item { NewPlaylistRow(onClick = { showCreate = true }) }
                    items(playlists, key = { it.id }) { pl ->
                        PlaylistPickRow(
                            playlist = pl,
                            added = song.title in pl.songTitles,
                            onClick = { onToggle(pl) },
                        )
                    }
                }
            }
        }
    }
}

/** Ligne "+ Nouvelle playlist" en tête de liste. */
@Composable
private fun NewPlaylistRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Aurora.Purple.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(R.drawable.ic_add), contentDescription = null, tint = Aurora.Purple)
        }
        Spacer(Modifier.width(14.dp))
        Text(
            stringResource(R.string.create_playlist_title),
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

/** Ligne d'une playlist : vignette + titre + compteur, avec "+" ou pastille validée. */
@Composable
private fun PlaylistPickRow(playlist: Playlist, added: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Aurora.AccentBrush),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painterResource(R.drawable.ic_playlist),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp),
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                playlist.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                stringResource(R.string.playlist_song_count, playlist.songTitles.size),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            )
        }
        if (added) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Aurora.Purple),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painterResource(R.drawable.ic_check),
                    contentDescription = stringResource(R.string.cd_already_added),
                    tint = Color.White,
                    modifier = Modifier.size(18.dp),
                )
            }
        } else {
            Icon(
                painterResource(R.drawable.ic_add),
                contentDescription = stringResource(R.string.cd_add),
                tint = Aurora.Purple,
            )
        }
    }
}
