package com.example.contentproviderformusic

import android.graphics.BitmapFactory
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongScreen(song: Song, isPlaying:Boolean, onPrevious:()->Unit,onNext:()->Unit,onPlayPause:()->Unit, sliderValue:Float, onSliderValueChanged:(Float)->Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal = 12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        AlbumImage(data = song.data, imageSize = 300.dp, clipSize = 16.dp)
        Spacer(Modifier.height(32.dp))
        Text(text = song.title, textAlign = TextAlign.Center, modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(Modifier.height(16.dp))
        Row {
            IconButton(onClick = onPrevious) {
                Icon(painter = painterResource(R.drawable.previous_icon), contentDescription = null)
            }
            IconButton(onClick = onPlayPause) {
                Icon(painter = if(isPlaying) painterResource(R.drawable.pause_icon) else painterResource(R.drawable.play_icon), contentDescription = null)
            }
            IconButton(onClick = onNext) {
                Icon(painter = painterResource(R.drawable.next_icon), contentDescription = null)
            }
        }
        Row {
            Text(text = formatDuration(sliderValue), modifier = Modifier.weight(0.2f))
            Slider(value = sliderValue, colors = SliderDefaults.colors(inactiveTrackColor = Color.Red,activeTrackColor = Color.Cyan),
                thumb = {
                    SliderDefaults.Thumb(colors = SliderDefaults.colors(), enabled = true,
                thumbSize = DpSize(25.dp, 25.dp), interactionSource = remember { MutableInteractionSource() }
            ) },
                track = { sliderState ->
                    SliderDefaults.Track(
                        modifier = Modifier.height(12.dp),
                        sliderState = sliderState,
                        thumbTrackGapSize = 0.dp,
                    )
                },
                onValueChange = onSliderValueChanged, valueRange = 0f..song.duration.toFloat(), modifier = Modifier.weight(1f))
            Text(text = formatDuration(song.duration), modifier = Modifier.weight(0.2f))
        }
    }
}

@Composable
fun AlbumImage(data:String, imageSize: Dp, clipSize:Dp) {
    Box(modifier = Modifier.clip(RoundedCornerShape(clipSize))) {
        val context = LocalContext.current
        val imgArt = getImgArt(data)
        val image = if (imgArt != null) {
            BitmapFactory.decodeByteArray(imgArt, 0, imgArt.size)
        } else {
            BitmapFactory.decodeResource(context.resources, R.drawable.music_player_icon_slash_screen)
        }
        AsyncImage(
            modifier = Modifier.size(imageSize),
            model = image,
            contentDescription = null,
            contentScale = ContentScale.FillHeight
        )
    }
}