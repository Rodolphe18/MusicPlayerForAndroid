package com.francotte.contentproviderformusic.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

object Aurora {
    val Night = Color(0xFF0B1026)
    val Purple = Color(0xFF3B2A82)
    val Teal = Color(0xFF2EC4B6)
    val Cyan = Color(0xFF4DD6FF)

    val BarBrush = Brush.linearGradient(listOf(Night, Purple, Teal))
    val AccentBrush = Brush.linearGradient(listOf(Purple, Teal))
}