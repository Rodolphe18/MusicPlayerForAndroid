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
    val currentDuration: MutableStateFlow<Float> = MutableStateFlow(0f)
    val currentIndex: MutableStateFlow<Int> = MutableStateFlow(0)
    var screenStatus = MutableStateFlow(ScreenStatus.MAIN_SCREEN)

    // File de lecture réellement chargée dans le player. Elle vaut la bibliothèque complète
    // ou la sous-liste des favoris selon l'écran depuis lequel on lance la lecture — c'est
    // elle qui définit ce que font suivant/précédent.
    private val _playQueue = MutableStateFlow<List<Song>>(emptyList())

    private var controller: MediaController? = null

    private var progressJob: Job? = null

    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            isPlaying.value = player.isPlaying
            currentIndex.value = player.currentMediaItemIndex
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

    // Chanson actuellement en lecture, dérivée de la FILE de lecture (et non de la biblio),
    // bornée à l'index courant, avec son état favori réappliqué depuis les préférences.
    // La carte du player affiche CETTE chanson, quel que soit l'onglet affiché.
    val currentPlayingSong: StateFlow<Song?> = combine(
        _playQueue,
        currentIndex,
        userDataRepository.userData
    ) { queue, idx, userData ->
        queue.getOrNull(idx)?.let { it.copy(isFavorite = it.title in userData.favoritesSongs) }
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


    // Lance la lecture à partir d'une liste donnée (bibliothèque ou favoris) et d'un index
    // LOCAL à cette liste. La liste devient la file de lecture, donc suivant/précédent
    // naviguent à l'intérieur d'elle. On ne recharge la file que si elle a changé.
    fun playFromList(list: List<Song>, index: Int) {
        viewModelScope.launch {
            val controller = this@MainViewModel.controller ?: return@launch
            if (index !in list.indices) return@launch
            val sameQueue = _playQueue.value.map { it.uri } == list.map { it.uri }
            if (!sameQueue || controller.mediaItemCount == 0) {
                _playQueue.value = list
                controller.setMediaItems(list.map { it.toMediaItem() })
                controller.prepare()
            }
            controller.seekTo(index, 0)
            controller.play()
        }
    }

    // Utilisé à l'initialisation : lecture depuis la bibliothèque complète.
    fun playSelectedSong(index: Int) = playFromList(SongsFetcherRepository.songs, index)
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