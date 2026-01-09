package com.example.contentproviderformusic.ui.composable

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongAppBar(
    text: String,
    leftIcon: ImageVector,
    rightIcon: ImageVector,
    actionIconContentDescription: String? = null,
    modifier: Modifier = Modifier,
    colors: TopAppBarColors = TopAppBarDefaults.centerAlignedTopAppBarColors(
        containerColor = Color(
            0xFFD2B48C
        )
    ),
    onActionClick: () -> Unit = {},
    onNavigationClick: () -> Unit = {}
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = text,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF8B4513),
                maxLines = 1
            )
        },
        actions = {
            IconButton(onClick = onActionClick) {
                Icon(
                    imageVector = rightIcon,
                    contentDescription = actionIconContentDescription,
                    tint = Color(0xFF8B4513)
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigationClick) {
                Icon(
                    imageVector = leftIcon,
                    contentDescription = null,
                    tint = Color(0xFF8B4513)
                )
            }
        },
        colors = colors,
        modifier = modifier,
    )
}