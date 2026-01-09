package com.example.contentproviderformusic.ui

import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.FlagSet
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import com.example.contentproviderformusic.model.Song
import com.example.contentproviderformusic.repository.UserDataRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    // --- Permissions
    private val _permissionsGranted = MutableStateFlow(false)
    val permissionGranted = _permissionsGranted.asStateFlow()

    // --- UI state
    val isPlaying = MutableStateFlow(false)
    val isLoading = MutableStateFlow(true)
    val currentSong: MutableStateFlow<Song?> = MutableStateFlow(null)
    val currentDuration: MutableStateFlow<Float> = MutableStateFlow(0f)
    val currentIndex: MutableStateFlow<Int> = MutableStateFlow(0)
    var screenStatus = MutableStateFlow(ScreenStatus.MAIN_SCREEN)

    private var controller: MediaController? = null

    private var progressJob: Job? = null

    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            isPlaying.value = player.isPlaying
            currentIndex.value = player.currentMediaItemIndex
            val idx = player.currentMediaItemIndex
            if (idx in UserDataRepository.songs.indices) {
                currentSong.value = UserDataRepository.songs[idx]
            }
        }
    }

    fun startProgressUpdates() {
        if (progressJob != null) return
        progressJob = viewModelScope.launch {
            while (isActive) {
                controller?.let { c ->
                    currentDuration.value = c.currentPosition.toFloat()
                }
                delay(200)
            }
        }
    }

    fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
    }


    @OptIn(UnstableApi::class)
    fun attachController(ctrl: MediaController) {
        controller = ctrl
        ctrl.addListener(listener)
        startProgressUpdates()
        listener.onEvents(ctrl, Player.Events(FlagSet.Builder().build()))
        isLoading.value = false
    }

    fun detachController() {
        controller?.removeListener(listener)
        stopProgressUpdates()
        controller = null
    }

    fun updatePermissionStatus() {
        _permissionsGranted.value = true
    }

    fun playPause() = controller?.let { if (it.isPlaying) it.pause() else it.play() }
    fun nextSong() = controller?.seekToNext()
    fun prevSong() = controller?.seekToPrevious()
    fun stopSong() = controller?.stop()

    fun onSeekBarValueChanged(progress: Float) = controller?.seekTo((progress.toLong().coerceAtLeast(0L)))

    /** Lance la lecture de la chanson d’index [index]. */
    fun playSelectedSong(index: Int) {
        viewModelScope.launch {
            val controller = this@MainViewModel.controller ?: return@launch
            if (controller.mediaItemCount == 0) {
                val items = UserDataRepository.songs.map { it.toMediaItem() }
                controller.setMediaItems(items)
                controller.prepare()
            }
            controller.seekTo(index,0)
            controller.play()

        }
    }
}


private fun Song.toMediaItem(): MediaItem =
    MediaItem.Builder()
        .setUri(uri) // ex: content:// ou file://
        .setMediaMetadata(
            androidx.media3.common.MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist)
                .build()
        )
        .build()

enum class ScreenStatus { SONG_SCREEN, MAIN_SCREEN }