package com.francotte.contentproviderformusic.ui.composable

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.francotte.contentproviderformusic.model.Song

@Composable
fun CurrentSongBarTitle(modifier: Modifier= Modifier,song: Song) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {

        Text(
            modifier = Modifier.basicMarquee(),
            text = song.title,
            color = Color.White,
            maxLines = 1
        )
    }
}