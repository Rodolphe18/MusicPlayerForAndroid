package com.francotte.contentproviderformusic.ui.composable

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.francotte.contentproviderformusic.model.Song
import com.francotte.contentproviderformusic.utils.formatDuration


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrentSongBarSlider(song: Song, sliderValue: Float,
                         onSliderValueChanged: (Float) -> Unit) {
    val sliderColors = SliderDefaults.colors(
        thumbColor = Color.White,
        activeTrackColor = Color.White,
        inactiveTrackColor = Color.White.copy(alpha = 0.35f),
        activeTickColor = Color.Transparent,
        inactiveTickColor = Color.Transparent
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
        Spacer(Modifier.width(4.dp))
        Text(
            modifier = Modifier.weight(0.15f),
            text = formatDuration(sliderValue),
            fontSize = 9.sp,
            color = Color.White
        )
        Slider(
            modifier = Modifier.weight(1.4f),
            value = sliderValue,
            onValueChange = onSliderValueChanged,
            valueRange = 0f..song.duration.toFloat(),
            colors = sliderColors,
            thumb = {
                SliderDefaults.Thumb(
                    interactionSource = remember { MutableInteractionSource() },
                    enabled = true,
                    colors = sliderColors,
                    thumbSize = DpSize(16.dp, 16.dp)
                )
            },
            track = { sliderState ->
                SliderDefaults.Track(
                    modifier = Modifier.height(2.dp),
                    sliderState = sliderState,
                    thumbTrackGapSize = 0.dp,
                    colors = sliderColors
                )
            }
        )
        Text(
            modifier = Modifier.weight(0.15f),
            text = formatDuration(song.duration),
            fontSize = 10.sp,
            color = Color.White
        )
        Spacer(Modifier.width(4.dp))
    }

}