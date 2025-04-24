package com.example.contentproviderformusic.ui.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.contentproviderformusic.model.Song
import com.example.contentproviderformusic.utils.formatDuration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(songs:List<Song>, onClick:(Int, Song)->Unit) {
    Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
        SongAppBar("My Music Player", Icons.Filled.Search, Icons.Filled.Settings)
    }) { innerPadding ->
        LazyColumn(state = rememberLazyListState(), contentPadding = innerPadding) {
            itemsIndexed(songs) { index, song ->
                    Row(modifier = Modifier.padding(horizontal = 8.dp).clickable {
                        onClick(index, song)
                    }, verticalAlignment = Alignment.CenterVertically) {
                        AlbumImage(Modifier.size(50.dp),song.data,  8.dp)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .weight(1f)
                                .height(70.dp)
                                .padding(start = 14.dp, end = 8.dp, bottom = 2.dp, top = 2.dp)
                        ) {
                            Text(
                                text = song.title,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = formatDuration(song.duration),
                            modifier = Modifier.weight(0.3f)
                        )
                    }
                }
            }
    }
}


