package com.francotte.contentproviderformusic.ui.playlists

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
        containerColor = Aurora.CoralBackground,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_songs)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Aurora.CoralBackground,
                    scrolledContainerColor = Aurora.CoralBackground,
                    titleContentColor = Aurora.Night,
                    navigationIconContentColor = Aurora.Night,
                    actionIconContentColor = Aurora.Night,
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = stringResource(R.string.cd_back))
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Aurora.CoralBackground)
                .padding(innerPadding),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            itemsIndexed(songs, key = { index, song -> "${song.uri}#$index" }) { _, song ->
                val added = song.title in addedTitles
                PlaylistSongRow(
                    song = song,
                    trailing = {
                        if (added) {
                            IconButton(onClick = {}, enabled = false) {
                                Icon(
                                    painterResource(R.drawable.ic_check),
                                    contentDescription = stringResource(R.string.cd_already_added),
                                    tint = Aurora.Teal,
                                )
                            }
                        } else {
                            IconButton(onClick = { onAdd(song.title) }) {
                                Icon(
                                    painterResource(R.drawable.ic_add),
                                    contentDescription = stringResource(R.string.cd_add),
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
