package com.example.contentproviderformusic

import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun SongScreen(song: Song) {

    Column {
        AlbumImage(data = song.data, imageSize = 300.dp, clipSize = 16.dp)
        Spacer(Modifier.height(32.dp))
        Text(song.title)
        Spacer(Modifier.height(16.dp))

    }
}

@Composable
fun AlbumImage(data:String, imageSize: Dp, clipSize:Dp) {
    Box(modifier = Modifier.clip(RoundedCornerShape(clipSize))) {
        val context = LocalContext.current
        val imgArt = getImgArt(data)
        val image = if (imgArt != null) {
            BitmapFactory.decodeByteArray(imgArt, 0, imgArt.size)
        } else {
            BitmapFactory.decodeResource(context.resources, R.drawable.music_player_icon_slash_screen)
        }
        AsyncImage(
            modifier = Modifier.size(imageSize),
            model = image,
            contentDescription = null,
            contentScale = ContentScale.FillHeight
        )
    }
}