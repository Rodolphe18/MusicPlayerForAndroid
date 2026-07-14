package com.francotte.contentproviderformusic.ui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.francotte.contentproviderformusic.ui.theme.Aurora

/**
 * Champ de saisie "liquid glass" clair : BasicTextField habillé d'un decorationBox à
 * fond translucide légèrement dégradé (blanc → teintes Aurora douces), texte sombre.
 */
@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minHeight: Dp = 0.dp,
) {
    val shape = RoundedCornerShape(18.dp)
    // Une seule couleur (violet Aurora) avec un léger dégradé vertical, ton plus prononcé.
    val glass = Brush.verticalGradient(
        listOf(
            Aurora.Purple.copy(alpha = 0.26f),
            Aurora.Purple.copy(alpha = 0.15f),
        ),
    )
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
        cursorBrush = SolidColor(Aurora.Purple),
        modifier = modifier,
        decorationBox = { inner ->
            Box(
                modifier = Modifier
                    .clip(shape)
                    .background(glass)
                    .border(1.dp, Aurora.Purple.copy(alpha = 0.30f), shape)
                    .heightIn(min = minHeight)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                contentAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart,
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                inner()
            }
        },
    )
}

/** Bouton plein d'une seule couleur (violet Aurora) avec un léger dégradé. */
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    horizontalPadding: Dp = 32.dp,
) {
    val shape = RoundedCornerShape(16.dp)
    val brush = if (enabled) {
        Brush.horizontalGradient(listOf(Color(0xFF4C3A9E), Color(0xFF362578)))
    } else {
        Brush.horizontalGradient(listOf(Color(0xFFB9B4C7), Color(0xFFA7A2BC)))
    }
    Box(
        modifier = modifier
            .clip(shape)
            .background(brush)
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = horizontalPadding, vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
