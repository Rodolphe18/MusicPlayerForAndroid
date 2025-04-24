package com.example.contentproviderformusic

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage


@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SongScreen(
    song: Song,
    isPlaying: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onPlayPause: () -> Unit,
    sliderValue: Float,
    onSliderValueChanged: (Float) -> Unit,
    onNavigationClick: () -> Unit
) {
    Scaffold(topBar = {
        SongAppBar(song.title, Icons.AutoMirrored.Filled.ArrowBack, Icons.Filled.Favorite, onNavigationClick = onNavigationClick)
    }) {
        SongBody(
            song,
            isPlaying,
            onPrevious,
            onNext,
            onPlayPause,
            sliderValue,
            onSliderValueChanged
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongBody(
    song: Song,
    isPlaying: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onPlayPause: () -> Unit,
    sliderValue: Float,
    onSliderValueChanged: (Float) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AlbumImage(data = song.data, modifier = Modifier.size(300.dp), clipSize = 16.dp)
        Spacer(Modifier.height(36.dp))
        Text(
            text = song.title,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(36.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD2B48C)),
                onClick = onPrevious
            ) {
                Icon(painter = painterResource(R.drawable.previous_icon), contentDescription = null)
            }
            Spacer(Modifier.width(24.dp))
            IconButton(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD2B48C)), onClick = onPlayPause
            ) {
                Icon(
                    painter = if (isPlaying) painterResource(R.drawable.pause_icon) else painterResource(
                        R.drawable.play_icon
                    ), contentDescription = null
                )
            }
            Spacer(Modifier.width(24.dp))
            IconButton(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD2B48C)), onClick = onNext
            ) {
                Icon(painter = painterResource(R.drawable.next_icon), contentDescription = null)
            }
        }
        Spacer(Modifier.height(36.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(modifier = Modifier.weight(0.2f), text = formatDuration(sliderValue))
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
                        thumbSize = DpSize(15.dp, 15.dp),
                        interactionSource = remember { MutableInteractionSource() }
                    )
                },
                track = { sliderState ->
                    SliderDefaults.Track(
                        modifier = Modifier.height(6.dp),
                        sliderState = sliderState,
                        thumbTrackGapSize = 0.dp,
                    )
                })
            Text(
                modifier = Modifier
                    .weight(0.2f)
                    .padding(start = 4.dp),
                text = formatDuration(song.duration)
            )
        }
    }
}

@Composable
fun AlbumImage(modifier: Modifier=Modifier, data: String, clipSize: Dp=0.dp) {
    Box(modifier = Modifier.clip(RoundedCornerShape(clipSize))) {
        val context = LocalContext.current
        val imgArt = getImgArt(data)
        val image = if (imgArt != null) {
            BitmapFactory.decodeByteArray(imgArt, 0, imgArt.size)
        } else {
            BitmapFactory.decodeResource(
                context.resources,
                R.drawable.music_player_icon_slash_screen
            )
        }
        AsyncImage(
            modifier = modifier,
            model = image,
            contentDescription = null,
            contentScale = ContentScale.FillHeight
        )
    }
}

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