package com.francotte.contentproviderformusic.ui.composable

import android.content.ComponentName
import android.content.Context
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.francotte.contentproviderformusic.service.MusicService
import kotlinx.coroutines.guava.await


@OptIn(UnstableApi::class)
@Composable
fun rememberMediaController(context: Context): MediaController? {
    var controller by remember { mutableStateOf<MediaController?>(null) }

    LaunchedEffect(Unit) {
        val token = SessionToken(context, ComponentName(context, MusicService::class.java))
        controller = MediaController.Builder(context, token).buildAsync().await()
    }

    DisposableEffect(Unit) {
        onDispose { controller?.release() }
    }
    return controller
}