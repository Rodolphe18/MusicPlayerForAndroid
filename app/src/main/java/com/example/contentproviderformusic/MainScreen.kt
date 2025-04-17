package com.example.contentproviderformusic

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.rememberImagePainter
import coil.decode.SvgDecoder
import coil.decode.VideoFrameDecoder
import java.util.concurrent.TimeUnit

@Composable
fun MainScreen(songs:List<Song>) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        LazyColumn(state = rememberLazyListState(), contentPadding = innerPadding) {
            items(songs) { song ->
                Column {
                    Row(modifier = Modifier.clickable {
//                        Intent(
//                            applicationContext,
//                            MainService::class.java
//                        ).also {
//                            it.action = MainService.Actions.START.toString()
//                            it.putExtra("SONG_ID", song.uri.toString())
//                            startService(it)
//                        }
                    }, verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .weight(0.25f)
                                .padding(
                                    start = 14.dp,
                                    end = 6.dp,
                                    top = 4.dp,
                                    bottom = 4.dp
                                )
                        ) {
                            AsyncImage(
                                error = rememberImagePainter(R.drawable.music_player_icon_slash_screen),
                                placeholder = rememberImagePainter(R.drawable.music_player_icon_slash_screen),
                                modifier = Modifier.size(50.dp),
                                model = song.albumImage,
                                contentDescription = null,
                                imageLoader =
                                ImageLoader.Builder(LocalContext.current)
                                    .components {
                                        add(VideoFrameDecoder.Factory())
                                        add(SvgDecoder.Factory())
                                    }.build()
                            )
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .height(70.dp)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
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
}

fun formatDuration(duration: Long): String {
    val minutes = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
    val seconds = (TimeUnit.SECONDS.convert(duration, TimeUnit.MILLISECONDS) -
            minutes * TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES))
    return String.format("%02d:%02d", minutes, seconds)
}
