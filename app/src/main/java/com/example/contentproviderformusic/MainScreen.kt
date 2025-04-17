package com.example.contentproviderformusic

import android.graphics.BitmapFactory
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import java.util.concurrent.TimeUnit

@Composable
fun MainScreen(songs:List<Song>, onClick:(Song)->Unit) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        LazyColumn(state = rememberLazyListState(), contentPadding = innerPadding) {
            items(songs) { song ->
                    Row(modifier = Modifier.padding(horizontal = 8.dp).clickable {
                        onClick(song)
//                        Intent(
//                            applicationContext,
//                            MainService::class.java
//                        ).also {
//                            it.action = MainService.Actions.START.toString()
//                            it.putExtra("SONG_ID", song.uri.toString())
//                            startService(it)
//                        }
                    }, verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.clip(RoundedCornerShape(8.dp))) {
                            val context = LocalContext.current
                            val imgArt = getImgArt(song.data)
                            val image = if (imgArt != null) {
                                BitmapFactory.decodeByteArray(imgArt, 0, imgArt.size)
                            } else {
                                BitmapFactory.decodeResource(context.resources, R.drawable.music_player_icon_slash_screen)
                            }
                            AsyncImage(
                                error = rememberAsyncImagePainter(R.drawable.music_player_icon_slash_screen),
                                placeholder = rememberAsyncImagePainter(R.drawable.music_player_icon_slash_screen),
                                modifier = Modifier.size(50.dp),
                                model = image,
                                contentDescription = null,
                                contentScale = ContentScale.FillHeight
                            )
                        }
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

fun formatDuration(duration: Long): String {
    val minutes = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
    val seconds = (TimeUnit.SECONDS.convert(duration, TimeUnit.MILLISECONDS) -
            minutes * TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES))
    return String.format("%02d:%02d", minutes, seconds)
}
