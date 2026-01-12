package com.francotte.contentproviderformusic.ui.composable

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.francotte.contentproviderformusic.R
import com.francotte.contentproviderformusic.model.Song
import com.francotte.contentproviderformusic.ui.theme.Aurora
import com.francotte.contentproviderformusic.utils.formatDuration
import com.francotte.contentproviderformusic.utils.getImgArt


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
    onNavigationClick: () -> Unit,
    onVerticalDrag: () -> Unit
) {
    Scaffold(topBar = {
        SongAppBar(
            modifier = Modifier,
            song.title,
            Icons.AutoMirrored.Filled.ArrowBack,
            Icons.Filled.Favorite,
            onNavigationClick = onNavigationClick
        )
    }) {
        SongBody(
            song,
            isPlaying,
            onPrevious,
            onNext,
            onPlayPause,
            sliderValue,
            onSliderValueChanged,
            onVerticalDrag
        )
    }
}

enum class DragAnchors {
    Start,
    End,
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SongBody(
    song: Song,
    isPlaying: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onPlayPause: () -> Unit,
    sliderValue: Float,
    onSliderValueChanged: (Float) -> Unit,
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Aurora.Cyan.copy(0.1f))
            .padding(horizontal = 12.dp),
        //    .anchoredDraggable(state = state, orientation = Orientation.Vertical),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AlbumImage(data = song.data, modifier = Modifier.size(300.dp), clipSize = 16.dp)
        Spacer(Modifier.height(36.dp))
        Text(
            text = song.title,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            color = Aurora.Night
        )
        Spacer(Modifier.height(36.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Aurora.Purple.copy(0.9f)),
                onClick = onPrevious
            ) {
                Icon(painter = painterResource(R.drawable.previous_icon), contentDescription = null, tint = Color.White)
            }
            Spacer(Modifier.width(24.dp))
            IconButton(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Aurora.Purple.copy(0.9f)), onClick = onPlayPause
            ) {
                Icon(
                    painter = if (isPlaying) painterResource(R.drawable.pause_icon) else painterResource(
                        R.drawable.play_icon
                    ), contentDescription = null, tint = Color.White
                )
            }
            Spacer(Modifier.width(24.dp))
            IconButton(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Aurora.Purple.copy(0.9f)), onClick = onNext
            ) {
                Icon(painter = painterResource(R.drawable.next_icon), contentDescription = null, tint = Color.White)
            }
        }
        Spacer(Modifier.height(36.dp))
        CustomSlider(song, Aurora.Purple,Aurora.Purple, Aurora.Purple,sliderValue,onSliderValueChanged)
    }
}

@Composable
fun AlbumImage(modifier: Modifier = Modifier, data: String, clipSize: Dp = 0.dp) {
    val imgArt = getImgArt(data)
    val image = if (imgArt != null) {
        BitmapFactory.decodeByteArray(imgArt, 0, imgArt.size)
    } else {
       null
    }
    val imagePainter = rememberAsyncImagePainter(image)
    Box(
        modifier = Modifier.clip(RoundedCornerShape(clipSize))
    ) {
        Image(
            modifier = if (imgArt == null) modifier.background(Aurora.Teal) else modifier,
            painter = if (image != null) imagePainter else painterResource(R.drawable.ic_person),
            contentDescription = null,
            contentScale = ContentScale.FillHeight,
        )
    }
}

