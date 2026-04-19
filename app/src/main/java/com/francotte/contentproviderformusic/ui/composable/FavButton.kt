package com.francotte.contentproviderformusic.ui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.francotte.contentproviderformusic.R

@Composable
fun FavButton(
    modifier: Modifier = Modifier,
    isFavorite:Boolean,
    onToggleFavorite: () -> Unit
) {

    val backgroundColor = if (isFavorite) colorResource(R.color.purple_200) else Color.White
    val iconColor = if (isFavorite) Color.White else Color.LightGray

    Box(
        modifier =
            modifier
                .size(55.dp)
                .background(backgroundColor, CircleShape)
                .clip(CircleShape)
                .toggleable(
                    value = isFavorite,
                    onValueChange = { onToggleFavorite() },
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ),
    ) {
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            modifier = Modifier.align(Alignment.Center).size(25.dp),
            tint = iconColor,
        )
    }
}
