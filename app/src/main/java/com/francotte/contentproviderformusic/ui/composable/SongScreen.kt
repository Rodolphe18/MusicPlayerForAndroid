package com.francotte.contentproviderformusic.ui.composable

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.francotte.contentproviderformusic.R
import com.francotte.contentproviderformusic.model.Song
import com.francotte.contentproviderformusic.ui.theme.Aurora
import com.francotte.contentproviderformusic.utils.getImgArt


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SongBody(
    song: Song,
    isPlaying: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onPlayPause: () -> Unit,
    sliderValue: Float,
    onSliderValueChanged: (Float) -> Unit,
    onToggleFavorite: (String, Boolean) -> Unit,
    onAddToPlaylist: () -> Unit = {},
    isRepeatOneEnabled: Boolean = false,
    onToggleRepeatOne: () -> Unit = {},
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Aurora.Cyan.copy(0.1f))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ScreenAlbumImage(
            data = song.data,
            title = song.title,
            modifier = Modifier.size(300.dp),
            clipSize = 16.dp,
            isFavorite = song.isFavorite,
            onToggleFavorite = { onToggleFavorite(song.title, !song.isFavorite) },
            onAddToPlaylist = onAddToPlaylist,
            onShare = { shareSong(context, song) })
        Spacer(Modifier.height(36.dp))
        Text(
            text = song.title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            maxLines = 2,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            color = Color.White
        )
        Spacer(Modifier.height(36.dp))
        // Contrôles de lecture façon Deezer : icônes pleines, grand play/pause central.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(
                modifier = Modifier.size(40.dp),
                onClick = onToggleRepeatOne,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_repeat),
                    contentDescription = null,
                    tint = if (isRepeatOneEnabled) Aurora.VividCoral else Color.White,
                    modifier = Modifier.size(22.dp),
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                IconButton(
                    modifier = Modifier.size(56.dp),
                    onClick = onPrevious,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_skip_previous),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(48.dp),
                    )
                }
                IconButton(
                    modifier = Modifier.size(80.dp),
                    onClick = onPlayPause,
                ) {
                    Icon(
                        painter = if (isPlaying) painterResource(R.drawable.ic_pause) else painterResource(
                            R.drawable.ic_play_arrow
                        ),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(64.dp),
                    )
                }
                IconButton(
                    modifier = Modifier.size(56.dp),
                    onClick = onNext,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_skip_next),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(48.dp),
                    )
                }
            }
            Icon(
                painter = painterResource(R.drawable.ic_shuffle),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(Modifier.height(36.dp))
        CustomSlider(song, Color.White, Color.White, Color.White, sliderValue, onSliderValueChanged)
    }
}

@Composable
fun ItemAlbumImage(modifier: Modifier = Modifier, data: String, title: String, clipSize: Dp = 0.dp) {
    val imgArt = getImgArt(data)
    val image = if (imgArt != null) {
        BitmapFactory.decodeByteArray(imgArt, 0, imgArt.size)
    } else {
        null
    }
    val imagePainter = rememberAsyncImagePainter(image)
    Box(
        modifier = Modifier.clip(RoundedCornerShape(clipSize))
    ) {
        if (image != null) {
            Image(
                modifier = modifier,
                painter = imagePainter,
                contentDescription = null,
                contentScale = ContentScale.FillHeight,
            )
        } else {
            // Pas de pochette : avatar avec les 2 premières lettres du titre, fond coloré
            // (jaune/orange/rouge) déterminé par la 1re lettre.
            Box(
                modifier = modifier.background(initialsColor(title)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = initialsOf(title),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

/** Les 2 premières lettres du titre, en majuscules (repli sur "?" si vide). */
private fun initialsOf(title: String): String {
    val cleaned = title.trim()
    return if (cleaned.isEmpty()) "?" else cleaned.take(2).uppercase()
}

/** Trois couleurs d'avatar (jaune, orange, rouge) associées à la 1re lettre du titre. */
private val InitialsColors = listOf(
    Color(0xFFEAB308), // jaune
    Color(0xFFF97316), // orange
    Color(0xFFEF4444), // rouge
)

private fun initialsColor(title: String): Color {
    val first = title.trim().firstOrNull()?.uppercaseChar() ?: 'A'
    return InitialsColors[first.code % InitialsColors.size]
}

@Composable
fun ScreenAlbumImage(
    modifier: Modifier = Modifier,
    data: String,
    title: String,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onAddToPlaylist: () -> Unit = {},
    onShare: () -> Unit = {},
    clipSize: Dp = 0.dp
) {
    val imgArt = getImgArt(data)
    val image = if (imgArt != null) {
        BitmapFactory.decodeByteArray(imgArt, 0, imgArt.size)
    } else {
        null
    }
    val imagePainter = rememberAsyncImagePainter(image)
    Box(
        modifier = Modifier.clip(RoundedCornerShape(clipSize))
    ) {
        if (image != null) {
            Image(
                modifier = modifier,
                painter = imagePainter,
                contentDescription = null,
                contentScale = ContentScale.FillHeight,
            )
        } else {
            // Pas de pochette : mêmes 2 initiales blanches et mêmes 3 couleurs que dans la
            // liste, avec un shimmer diagonal (haut-gauche -> bas-droite) en boucle.
            InitialsShimmerCover(title = title, modifier = modifier)
        }
        // Action au coin bas gauche : partager la chanson.
        ShareButton(
            onClick = onShare,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp),
        )
        // Actions au coin bas droit : "+" (ajouter à une playlist) à gauche du cœur.
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            AddToPlaylistButton(onClick = onAddToPlaylist)
            FavButton(
                isFavorite = isFavorite,
                onToggleFavorite = onToggleFavorite
            )
        }
    }
}

/** Partage la chanson (fichier audio + titre/artiste) via un sélecteur d'app système. */
private fun shareSong(context: Context, song: Song) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "audio/*"
        putExtra(Intent.EXTRA_STREAM, song.uri)
        putExtra(Intent.EXTRA_SUBJECT, song.title)
        putExtra(Intent.EXTRA_TEXT, "${song.title} — ${song.artist}")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(
        Intent.createChooser(sendIntent, context.getString(R.string.share_song))
    )
}

/** Bouton circulaire "partager" (assorti au FavButton / AddToPlaylistButton). */
@Composable
private fun ShareButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(55.dp)
            .background(Color.White, RoundedCornerShape(50))
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_share),
            contentDescription = stringResource(R.string.cd_share),
            modifier = Modifier.align(Alignment.Center).size(28.dp),
            tint = Aurora.Purple,
        )
    }
}

/**
 * Pochette de remplacement (pas d'image) : 2 initiales blanches sur fond coloré (mêmes
 * couleurs que la liste) + un reflet "shimmer" qui balaie l'écran en diagonale, en boucle.
 */
@Composable
private fun InitialsShimmerCover(title: String, modifier: Modifier = Modifier) {
    val bg = initialsColor(title)
    val transition = rememberInfiniteTransition(label = "cover-shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "cover-shimmer-progress",
    )
    Box(
        modifier = modifier.drawWithContent {
            drawRect(color = bg)
            // Bande claire centrée sur la diagonale, translatée de haut-gauche vers bas-droite.
            val band = size.minDimension * 0.55f
            val travel = size.width + size.height + band * 2f
            val shift = progress * travel - band
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0f),
                        Color.White.copy(alpha = 0.35f),
                        Color.White.copy(alpha = 0f),
                    ),
                    start = Offset(shift - band, shift - band),
                    end = Offset(shift + band, shift + band),
                )
            )
            drawContent()
        },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initialsOf(title),
            color = Color.White,
            fontSize = 96.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

/** Bouton circulaire "+" pour ajouter la chanson à une playlist (assorti au FavButton). */
@Composable
private fun AddToPlaylistButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(55.dp)
            .background(Color.White, RoundedCornerShape(50))
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_add),
            contentDescription = stringResource(R.string.add_to_playlist_title),
            modifier = Modifier.align(Alignment.Center).size(28.dp),
            tint = Aurora.Purple,
        )
    }
}

