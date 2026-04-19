package com.francotte.contentproviderformusic.ui.composable

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.francotte.contentproviderformusic.R
import com.francotte.contentproviderformusic.model.Song
import com.francotte.contentproviderformusic.ui.theme.Aurora
import com.francotte.contentproviderformusic.utils.getImgArt


//@OptIn(ExperimentalMaterial3Api::class)
//@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
//@Composable
//fun SongScreen(
//    song: Song,
//    isPlaying: Boolean,
//    onPrevious: () -> Unit,
//    onNext: () -> Unit,
//    onPlayPause: () -> Unit,
//    sliderValue: Float,
//    onSliderValueChanged: (Float) -> Unit,
//    onNavigationClick: () -> Unit
//) {
//    Scaffold(topBar = {
//        SongAppBar(
//            modifier = Modifier,
//            song.title,
//            Icons.AutoMirrored.Filled.ArrowBack,
//            Icons.Filled.Favorite,
//            onNavigationClick = onNavigationClick
//        )
//    }) {
//        SongBody(
//            song,
//            isPlaying,
//            onPrevious,
//            onNext,
//            onPlayPause,
//            sliderValue,
//            onSliderValueChanged
//        )
//    }
//}
//
//enum class DragAnchors {
//    Start,
//    End,
//}


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
    onToggleFavorite: (String, Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Aurora.Cyan.copy(0.1f))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ScreenAlbumImage(
            data = song.data,
            modifier = Modifier.size(300.dp),
            clipSize = 16.dp,
            isFavorite = song.isFavorite,
            onToggleFavorite = { onToggleFavorite(song.title, !song.isFavorite) })
        Spacer(Modifier.height(36.dp))
        Text(
            text = song.title,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            color = Color.White
        )
        Spacer(Modifier.height(36.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                modifier = Modifier.size(50.dp),
                onClick = onPrevious
            ) {
                Icon(
                    painter = painterResource(R.drawable.previous_icon),
                    contentDescription = null,
                    tint = Color.White
                )
            }
            Spacer(Modifier.width(24.dp))
            IconButton(
                modifier = Modifier.size(60.dp),
                onClick = onPlayPause
            ) {
                Icon(
                    painter = if (isPlaying) painterResource(R.drawable.pause_icon) else painterResource(
                        R.drawable.play_icon
                    ), contentDescription = null, tint = Color.White
                )
            }
            Spacer(Modifier.width(24.dp))
            IconButton(
                modifier = Modifier.size(50.dp), onClick = onNext
            ) {
                Icon(
                    painter = painterResource(R.drawable.next_icon),
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
        Spacer(Modifier.height(36.dp))
        CustomSlider(song, Color.White, Color.White, Color.White, sliderValue, onSliderValueChanged)
    }
}

@Composable
fun ItemAlbumImage(modifier: Modifier = Modifier, data: String, clipSize: Dp = 0.dp) {
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
            modifier = if (imgArt == null) modifier.background(Aurora.Purple.copy(0.4f)) else modifier,
            painter = if (image != null) imagePainter else painterResource(R.drawable.ic_person),
            contentDescription = null,
            contentScale = ContentScale.FillHeight,
        )
    }
}

@Composable
fun ScreenAlbumImage(
    modifier: Modifier = Modifier,
    data: String,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    clipSize: Dp = 0.dp
) {
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
            modifier = if (imgArt == null) modifier.background(Aurora.Purple.copy(0.4f)) else modifier,
            painter = if (image != null) imagePainter else painterResource(R.drawable.ic_person),
            contentDescription = null,
            contentScale = ContentScale.FillHeight,
        )
        FavButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp),
            isFavorite = isFavorite,
            onToggleFavorite = onToggleFavorite
        )
    }
}

