package com.francotte.contentproviderformusic.ui.playlists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.francotte.contentproviderformusic.R
import com.francotte.contentproviderformusic.model.Playlist
import com.francotte.contentproviderformusic.model.Song
import com.francotte.contentproviderformusic.ui.composable.GlassCard
import com.francotte.contentproviderformusic.ui.theme.Aurora

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playlist: Playlist?,
    songs: List<Song>,
    currentSong: Song?,
    onBack: () -> Unit,
    onAddSongsClick: () -> Unit,
    onPlay: (List<Song>, Int) -> Unit,
    onRemoveSong: (String) -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(playlist?.title ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = "Retour")
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // En-tête : card glass avec nom + description centrés.
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = playlist?.title.orEmpty(),
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                        )
                        if (!playlist?.description.isNullOrBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = playlist!!.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }

            // Rangée "ajouter des titres" : bouton circulaire glass + texte.
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAddSongsClick() }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    GlassCard(shape = CircleShape, modifier = Modifier.size(48.dp)) {
                        Icon(
                            painterResource(R.drawable.ic_add),
                            contentDescription = "Ajouter des titres",
                            tint = Color.White,
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                    Spacer(Modifier.size(12.dp))
                    Text("Ajouter des titres", style = MaterialTheme.typography.titleMedium)
                }
            }

            // Titres de la playlist.
            items(songs, key = { it.title }) { song ->
                val index = songs.indexOf(song)
                PlaylistSongRow(
                    song = song,
                    isCurrent = song.uri == currentSong?.uri,
                    onClick = { onPlay(songs, index) },
                    trailing = {
                        IconButton(onClick = { onRemoveSong(song.title) }) {
                            Icon(
                                painterResource(R.drawable.ic_delete),
                                contentDescription = "Retirer",
                                tint = Aurora.Purple,
                            )
                        }
                    },
                )
            }
        }
    }
}
