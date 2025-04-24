package com.example.contentproviderformusic.ui.composable

import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.contentproviderformusic.R
import com.example.contentproviderformusic.model.Song
import com.example.contentproviderformusic.utils.formatDuration


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CurrentSongBar(
    modifier: Modifier = Modifier,
    song: Song,
    isPlaying: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onPlayPause: () -> Unit,
    sliderValue: Float,
    onSliderValueChanged: (Float) -> Unit,
    onClick:()->Unit,
    onClose:()->Unit,
    onVerticalDrag: () -> Unit
) {
    val density = LocalDensity.current
    val state = remember {
        AnchoredDraggableState(
            // 2
            initialValue = DragAnchors.Start,
            // 3
            positionalThreshold = { distance: Float -> distance * 0.5f },
            // 4
            velocityThreshold = { with(density) { 100.dp.toPx() } },
            // 5
            snapAnimationSpec = tween(),
            decayAnimationSpec = exponentialDecay(),
            confirmValueChange = {
                onVerticalDrag()
                true
            }
        ).apply {
            // 6
            updateAnchors(
                // 7
                DraggableAnchors {
                    DragAnchors.Start at 0f
                    DragAnchors.End at 400f
                }
            )
        }
    }
    Column(modifier = modifier
        .fillMaxWidth()
        .height(135.dp)
        .border(Dp.Hairline, Color.Black)
        .background(Color.White)
        .clickable { onClick() }
        .anchoredDraggable(state, true,Orientation.Vertical)) {
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp)) {

            Text(
                modifier = Modifier.basicMarquee(),
                text = song.title,
                maxLines = 1
            )
        }
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Spacer(Modifier.width(20.dp))
            IconButton(
                modifier = Modifier.size(30.dp),
                onClick = onPrevious
            ) {
                Icon(
                    painter = painterResource(
                        R.drawable.previous_icon
                    ), contentDescription = null
                )
            }
            Spacer(Modifier.width(30.dp))
            IconButton(
                modifier = Modifier.size(30.dp), onClick = onPlayPause
            ) {
                Icon(
                    painter = if (isPlaying) painterResource(R.drawable.pause_icon) else painterResource(
                        R.drawable.play_icon
                    ), contentDescription = null
                )
            }
            Spacer(Modifier.width(30.dp))
            IconButton(
                modifier = Modifier.size(30.dp),
                onClick = onNext
            ) {
                Icon(
                    painter = painterResource(
                        R.drawable.next_icon
                    ), contentDescription = null
                )
            }
            Spacer(Modifier.width(30.dp))
            IconButton(
                modifier = Modifier.size(30.dp), onClick = onClose
            ) {
                Icon(
                    painter = painterResource(R.drawable.exit_icon), contentDescription = null
                )
            }
            Spacer(Modifier.width(40.dp))
            AlbumImage(data = song.data, modifier = Modifier.width(80.dp).height(45.dp), clipSize = 4.dp)

        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.width(8.dp))
            Text(modifier = Modifier.weight(0.2f), text = formatDuration(sliderValue), fontSize = 12.sp)
            Slider(
                modifier = Modifier.weight(1f),
                value = sliderValue,
                onValueChange = onSliderValueChanged,
                valueRange = 0f..song.duration.toFloat(),
                colors = SliderDefaults.colors(
                    inactiveTrackColor = Color.Red,
                    activeTrackColor = Color.Cyan
                ),
                thumb = {
                    SliderDefaults.Thumb(colors = SliderDefaults.colors(),
                        enabled = true,
                        thumbSize = DpSize(8.dp, 16.dp),
                        interactionSource = remember { MutableInteractionSource() }
                    )
                },
                track = { sliderState ->
                    SliderDefaults.Track(
                        modifier = Modifier.height(2.dp),
                        sliderState = sliderState,
                        thumbTrackGapSize = 0.dp,
                    )
                })
            Text(
                modifier = Modifier
                    .weight(0.2f)
                    .padding(start = 4.dp),
                text = formatDuration(song.duration),
                fontSize = 12.sp
            )
        }
    }

}