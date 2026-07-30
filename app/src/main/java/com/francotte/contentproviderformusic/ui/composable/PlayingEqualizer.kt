package com.francotte.contentproviderformusic.ui.composable

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.francotte.contentproviderformusic.ui.theme.Aurora.Coral

/**
 * Indicateur de lecture : petites barres blanches sur fond noir qui « pulsent » pour
 * mimer l'intensité du morceau. Remplace la pochette de la chanson en cours de lecture.
 *
 * @param animating true = barres animées (lecture) ; false = barres figées (pause).
 */
@Composable
fun PlayingEqualizer(
    modifier: Modifier = Modifier,
    animating: Boolean = true,
    cornerRadius: Dp = 16.dp,
    barColor: Color = Color.White,
    background: Color = Coral,
) {
    // Hauteurs cibles variées par barre pour un rendu organique.
    val restingHeights = listOf(0.35f, 0.7f, 0.5f, 0.85f)
    val durations = listOf(420, 300, 520, 360)

    val transition = rememberInfiniteTransition(label = "equalizer")
    val animatedHeights = durations.mapIndexed { index, duration ->
        transition.animateFloat(
            initialValue = 0.25f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = duration, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "bar$index",
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.Bottom,
        ) {
            restingHeights.forEachIndexed { index, resting ->
                val fraction = if (animating) animatedHeights[index].value else resting
                Box(
                    modifier = Modifier
                        .width(5.dp)
                        .fillMaxHeight(fraction)
                        .clip(RoundedCornerShape(2.dp))
                        .background(barColor),
                )
            }
        }
    }
}
