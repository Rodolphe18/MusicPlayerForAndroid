package com.francotte.contentproviderformusic.ui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.francotte.contentproviderformusic.ui.theme.Aurora

@Composable
fun AuroraBackground(content: @Composable () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F3EF)) // ton fond clair
    ) {
        // halo aurora en haut
        Box(
            Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Aurora.Teal.copy(alpha = 0.25f), Color.Transparent),
                        radius = 700f
                    )
                )
        )
        content()
    }
}