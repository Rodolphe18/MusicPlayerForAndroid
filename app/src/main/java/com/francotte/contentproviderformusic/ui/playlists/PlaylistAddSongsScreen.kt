package com.francotte.contentproviderformusic.ui.playlists

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.francotte.contentproviderformusic.R
import com.francotte.contentproviderformusic.model.Song
import com.francotte.contentproviderformusic.ui.theme.Aurora

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistAddSongsScreen(
    songs: List<Song>,
    addedTitles: Set<String>,
    onBack: () -> Unit,
    onAdd: (String) -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Ajouter des titres") },
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
            items(songs, key = { it.uri.toString() }) { song ->
                val added = song.title in addedTitles
                PlaylistSongRow(
                    song = song,
                    trailing = {
                        if (added) {
                            IconButton(onClick = {}, enabled = false) {
                                Icon(
                                    painterResource(R.drawable.ic_check),
                                    contentDescription = "Déjà ajouté",
                                    tint = Aurora.Teal,
                                )
                            }
                        } else {
                            IconButton(onClick = { onAdd(song.title) }) {
                                Icon(
                                    painterResource(R.drawable.ic_add),
                                    contentDescription = "Ajouter",
                                    tint = Aurora.Purple,
                                )
                            }
                        }
                    },
                )
            }
        }
    }
}
