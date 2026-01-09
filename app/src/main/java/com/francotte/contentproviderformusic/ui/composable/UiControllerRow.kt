package com.francotte.contentproviderformusic.ui.composable

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.francotte.contentproviderformusic.R

@Composable
fun UiControllerRow(isPlaying: Boolean,
                    onPrevious: () -> Unit,
                    onNext: () -> Unit,
                    onPlayPause: () -> Unit,onClose:()->Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        Spacer(Modifier.width(20.dp))
        IconButton(
            modifier = Modifier.size(40.dp).clip(CircleShape).border(1.dp, Color.White,CircleShape),
            onClick = onPrevious
        ) {
            Icon(
                painter = painterResource(
                    R.drawable.previous_icon
                ), contentDescription = null,tint = Color.White
            )
        }
        Spacer(Modifier.width(30.dp))
        IconButton(
            modifier = Modifier.size(40.dp).clip(CircleShape).border(1.dp, Color.White,CircleShape), onClick = onPlayPause
        ) {
            Icon(
                painter = if (isPlaying) painterResource(R.drawable.pause_icon) else painterResource(
                    R.drawable.play_icon
                ), contentDescription = null, tint = Color.White
            )
        }
        Spacer(Modifier.width(30.dp))
        IconButton(
            modifier = Modifier.size(40.dp).clip(CircleShape).border(1.dp, Color.White,CircleShape),
            onClick = onNext
        ) {
            Icon(
                painter = painterResource(
                    R.drawable.next_icon
                ), contentDescription = null,tint = Color.White
            )
        }
        Spacer(Modifier.width(30.dp))
        IconButton(
            modifier = Modifier.size(40.dp).clip(CircleShape).border(1.dp, Color.White,CircleShape), onClick = onClose
        ) {
            Icon(
                painter = painterResource(R.drawable.exit_icon), contentDescription = null,tint = Color.White
            )
        }
    }
}