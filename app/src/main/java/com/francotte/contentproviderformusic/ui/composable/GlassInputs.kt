package com.francotte.contentproviderformusic.ui.composable

import androidx.compose.foundation.background
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
    containerBrush: Brush? = null,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    placeholderColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
    cursorColor: Color = Aurora.Purple,
) {
    val shape = RoundedCornerShape(18.dp)
    // Fond par défaut : corail Aurora en léger dégradé. Surchargeable via [containerBrush].
    val background = containerBrush ?: Brush.verticalGradient(
        listOf(
            Aurora.Purple.copy(alpha = 0.26f),
            Aurora.Purple.copy(alpha = 0.15f),
        ),
    )
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = textColor),
        cursorBrush = SolidColor(cursorColor),
        modifier = modifier,
        decorationBox = { inner ->
            Box(
                modifier = Modifier
                    .clip(shape)
                    .background(background)
                    .heightIn(min = minHeight)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                contentAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart,
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = placeholderColor,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                inner()
            }
        },
    )
}

/** Bouton corail soutenu quand il est actif, corail clair quand il est désactivé. */
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    horizontalPadding: Dp = 32.dp,
) {
    val shape = RoundedCornerShape(16.dp)
    val brush: Brush = if (enabled) {
        SolidColor(Aurora.Coral)
    } else {
        SolidColor(Color(0xFFFFE1DE))
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
