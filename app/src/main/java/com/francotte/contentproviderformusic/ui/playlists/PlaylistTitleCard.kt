package com.francotte.contentproviderformusic.ui.playlists

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.francotte.contentproviderformusic.ui.theme.Aurora

/**
 * En-tête "poster typographique" : le titre de la playlist répété à plusieurs endroits
 * de la card (format 4/3) avec des tailles, styles, casses et couleurs différents.
 */
@Composable
fun PlaylistTitleCard(title: String, modifier: Modifier = Modifier) {
    val cream = Color(0xFFF3ECDD)
    val lilac = Color(0xFFE6B3F0)
    val display = title.ifBlank { "Playlist" }
    val shape = RoundedCornerShape(22.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(shape)
            .background(Brush.linearGradient(listOf(Aurora.Night, Aurora.Purple, Aurora.Night)))
            .border(1.dp, Color.White.copy(alpha = 0.12f), shape)
            .padding(18.dp),
    ) {
        // Filigrane géant, très faible, en haut.
        Text(
            text = display.uppercase(),
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Black,
            color = Color.White.copy(alpha = 0.06f),
            maxLines = 1,
            overflow = TextOverflow.Clip,
            modifier = Modifier.align(Alignment.TopStart).padding(bottom = 14.dp, start = 4.dp),
        )
        Text(
            text = display,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Black,
            color = Color.White.copy(alpha = 0.07f),
            maxLines = 1,
            overflow = TextOverflow.Clip,
            modifier = Modifier.align(Alignment.BottomCenter).padding(start = 14.dp, bottom = 28.dp),
        )

        // Petit, lilas, espacé, en haut à droite.
        Text(
            text = display.lowercase(),
            style = MaterialTheme.typography.labelLarge,
            letterSpacing = 4.sp,
            color = lilac,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.align(Alignment.TopEnd).padding(end = 14.dp, top = 8.dp),
        )

        // Titre principal, centré.
        Text(
            text = display,
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.ExtraBold,
            color = cream,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.align(Alignment.Center).fillMaxWidth(),
        )

        // Italique crème, en bas à gauche.
        Text(
            text = display.replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.titleMedium,
            fontStyle = FontStyle.Italic,
            color = cream.copy(alpha = 0.8f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.align(Alignment.BottomStart).padding(start = 16.dp),
        )

        // Gras lilas, MAJUSCULES, en bas à droite.
        Text(
            text = display.uppercase(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = lilac,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 12.dp, end = 2.dp),
        )
    }
}
