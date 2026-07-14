package com.francotte.contentproviderformusic.ui.playlists

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.francotte.contentproviderformusic.model.Song
import com.francotte.contentproviderformusic.ui.composable.ItemAlbumImage
import com.francotte.contentproviderformusic.ui.theme.Aurora

/**
 * Rangée de titre réutilisée par le détail (action retirer) et l'ajout (action +/coche).
 * [trailing] fournit l'action à droite.
 */
@Composable
fun PlaylistSongRow(
    song: Song,
    isCurrent: Boolean = false,
    onClick: (() -> Unit)? = null,
    trailing: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isCurrent) Aurora.Purple.copy(0.15f) else Color.White)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ItemAlbumImage(Modifier.size(50.dp), song.data, 16.dp)
        Column(
            modifier = Modifier
                .weight(1f)
                .height(70.dp)
                .padding(start = 14.dp, end = 8.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        trailing()
    }
}
