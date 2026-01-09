package com.francotte.contentproviderformusic.ui.composable

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.francotte.contentproviderformusic.model.Song

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(songs: List<Song>, currentIndex:Int, onClick: (Int, Song) -> Unit) {
    Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
        SongAppBar(Modifier, "My Music Player", Icons.Filled.Search, Icons.Filled.Settings)
    }) { innerPadding ->
        LazyColumn(state = rememberLazyListState(), contentPadding = innerPadding) {
            itemsIndexed(songs) { index, song ->
               SongItem(song, index == currentIndex) { onClick(index,song) }
            }
            item { Spacer(modifier = Modifier.height(90.dp)) }
        }
    }
}





