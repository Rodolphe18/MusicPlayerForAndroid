package com.francotte.contentproviderformusic.ui

import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.FlagSet
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import com.francotte.contentproviderformusic.data.UserDataRepository
import com.francotte.contentproviderformusic.domain.FavoritesUseCase
import com.francotte.contentproviderformusic.model.Song
import com.francotte.contentproviderformusic.repository.SongsFetcherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository,
    favoritesUseCase: FavoritesUseCase
) : ViewModel() {

    private val _permissionsGranted = MutableStateFlow(false)
    val permissionGranted = _permissionsGranted.asStateFlow()
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
            if (idx in SongsFetcherRepository.songs.indices) {
                currentSong.value = SongsFetcherRepository.songs[idx]
            }
        }
    }

    val favoritesSongs = favoritesUseCase.invoke().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), persistentListOf())

    // Liste complète des chansons, avec l'état favori dérivé des préférences persistées.
    // C'est cette liste (et non SongsFetcherRepository.songs brute) qui doit alimenter l'UI
    // pour que le FavButton reflète le bon état et se mette à jour au clic.
    val songs: StateFlow<ImmutableList<Song>> = userDataRepository.userData
        .map { userData ->
            SongsFetcherRepository.songs
                .map { it.copy(isFavorite = it.title in userData.favoritesSongs) }
                .toImmutableList()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), persistentListOf())

    // Chanson actuellement en lecture, avec son état favori, bornée à la liste réelle.
    // La carte du player doit afficher CETTE chanson, indépendamment de la liste affichée
    // par l'onglet (bibliothèque complète vs sous-liste de favoris) — sinon accès hors bornes.
    val currentPlayingSong: StateFlow<Song?> = combine(songs, currentIndex) { list, idx ->
        list.getOrNull(idx)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

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

    fun updateFavoritesSongs(songTitle: String, isFavorite: Boolean) {
        viewModelScope.launch {
            userDataRepository.setFavoritesSongs(songTitle, isFavorite)
        }
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

    fun onSeekBarValueChanged(progress: Float) =
        controller?.seekTo((progress.toLong().coerceAtLeast(0L)))


    // Joue une chanson identifiée par son uri, en résolvant son index réel dans la playlist
    // (SongsFetcherRepository.songs). Indispensable depuis l'écran Favoris, dont la liste
    // affichée est une sous-liste : son index local ne correspond pas à l'index de lecture.
    fun playSong(song: Song) {
        val index = SongsFetcherRepository.songs.indexOfFirst { it.uri == song.uri }
        if (index >= 0) playSelectedSong(index)
    }

    fun playSelectedSong(index: Int) {
        viewModelScope.launch {
            val controller = this@MainViewModel.controller ?: return@launch
            if (controller.mediaItemCount == 0) {
                val items = SongsFetcherRepository.songs.map { it.toMediaItem() }
                controller.setMediaItems(items)
                controller.prepare()
            }
            controller.seekTo(index, 0)
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