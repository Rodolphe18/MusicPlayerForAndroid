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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Six couleurs de playlist, utilisées successivement pour limiter les répétitions. */
val PlaylistAccents = listOf(
    Color(0xFF2F7D5F), // vert
    Color(0xFFFF3B30), // corail
    Color(0xFF63A66F), // vert clair
    Color(0xFFF2763D), // orange, entre jaune et corail
    Color(0xFFD89016), // ambre
    Color(0xFF168F9C), // turquoise
)

/** Couleur attribuée d'après la position de la playlist dans la liste. */
fun playlistAccentFor(index: Int): Color = PlaylistAccents[index % PlaylistAccents.size]

/**
 * En-tête "poster typographique" : le titre de la playlist répété à plusieurs endroits
 * de la card avec des tailles, styles, casses et couleurs différents. Le dégradé et les
 * filigranes se calent sur [color] (la couleur propre de la playlist).
 */
@Composable
fun PlaylistTitleCard(
    title: String,
    color: Color,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
) {
    val cream = Color(0xFFF3ECDD)
    val dark = lerp(color, Color.Black, 0.55f)
    val light = lerp(color, Color.White, 0.6f)
    val display = title.ifBlank { "Playlist" }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(2.2f)
            .clip(shape)
            .background(Brush.linearGradient(listOf(dark, color, dark)))
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
            modifier = Modifier.align(Alignment.BottomCenter).padding(start = 18.dp, bottom = 22.dp),
        )

        // Petit, clair, espacé, en haut à droite.
        Text(
            text = display.lowercase(),
            style = MaterialTheme.typography.labelLarge,
            letterSpacing = 4.sp,
            color = light.copy(alpha = 0.5f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.align(Alignment.TopEnd).padding(end = 14.dp, top = 8.dp),
        )

        // Titre principal, centré.
        Text(
            text = display,
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
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

        // Gras clair, MAJUSCULES, en bas à droite.
        Text(
            text = display.uppercase(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = light.copy(alpha = 0.5f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 12.dp, end = 8.dp)
        )
    }
}
