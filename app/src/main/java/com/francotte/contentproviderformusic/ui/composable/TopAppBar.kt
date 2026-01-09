package com.francotte.contentproviderformusic.ui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.francotte.contentproviderformusic.ui.theme.Aurora

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongAppBar(
    modifier: Modifier = Modifier,
    text: String,
    leftIcon: ImageVector,
    rightIcon: ImageVector,
    actionIconContentDescription: String? = null,
    onActionClick: () -> Unit = {},
    onNavigationClick: () -> Unit = {}
) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(Aurora.BarBrush)
    ) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = text,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 1
            )
        },
        actions = {
            IconButton(onClick = onActionClick) {
                Icon(
                    imageVector = rightIcon,
                    contentDescription = actionIconContentDescription,
                    tint = Color.White
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigationClick) {
                Icon(
                    imageVector = leftIcon,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent),
        modifier = modifier,
    )
}
}