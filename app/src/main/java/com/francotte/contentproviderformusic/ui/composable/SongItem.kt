package com.francotte.contentproviderformusic.ui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.francotte.contentproviderformusic.model.Song
import com.francotte.contentproviderformusic.ui.theme.Aurora
import com.francotte.contentproviderformusic.utils.formatDuration

@Composable
fun SongItem(song:Song, isCurrent:Boolean, onClick:()->Unit) {
    val bg = if (isCurrent) {
        Brush.linearGradient(
            listOf(
                Color.Transparent,
                Aurora.Night.copy(0.02f),
                Aurora.Purple.copy(0.12f),
            )
        )
    } else null
    Row(
        modifier = Modifier
            .background(bg ?: SolidColor(Color.White))
            .padding(horizontal = 8.dp)
            .clickable { onClick() }, verticalAlignment = Alignment.CenterVertically
    ) {
        AlbumImage(Modifier.size(50.dp), song.data, 8.dp)
        Column(
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
            modifier = Modifier.weight(0.3f).padding(end = 4.dp),
            textAlign = TextAlign.Right
        )
    }
}