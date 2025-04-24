package com.example.contentproviderformusic

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel : ViewModel() {

    private val _permissionsGranted = MutableStateFlow(false)
    val permissionGranted = _permissionsGranted.asStateFlow()

    val isPlaying = MutableStateFlow(false)

    val currentSong: MutableStateFlow<Song?> = MutableStateFlow(null)

    val currentDuration: MutableStateFlow<Float?> = MutableStateFlow(0f)

    var screenStatus = MutableStateFlow(ScreenStatus.MAIN_SCREEN)

    fun updatePermissionStatus() {
        _permissionsGranted.value = true
    }

}

enum class ScreenStatus {
    SONG_SCREEN, MAIN_SCREEN
}
