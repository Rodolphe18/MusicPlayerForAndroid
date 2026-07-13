package com.francotte.contentproviderformusic.ui.composable

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.francotte.contentproviderformusic.R
import com.francotte.contentproviderformusic.model.Song
import com.francotte.contentproviderformusic.ui.state.MusicAppState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    windowSizeClass: WindowSizeClass,
    appState: MusicAppState,
    songs: ImmutableList<Song>,
    currentSong: Song?,
    currentIndex: Int,
    isPlaying: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onPlayPause: () -> Unit,
    sliderValue: Float,
    onSliderValueChanged: (Float) -> Unit,
    onClose: () -> Unit,
    onPlay: (List<Song>, Int) -> Unit,
    onToggleFavorite: (String, Boolean) -> Unit,
    emptyContent: @Composable () -> Unit = {}
) {
    var searchActive by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }

    // Liste affichée = liste courante filtrée par la recherche (titre + artiste).
    val displayed = remember(query, songs) {
        if (query.isBlank()) {
            songs
        } else {
            songs.filter {
                it.title.contains(query, ignoreCase = true) ||
                    it.artist.contains(query, ignoreCase = true)
            }.toImmutableList()
        }
    }

    // Ferme la recherche au bouton retour système plutôt que de quitter l'écran.
    BackHandler(enabled = searchActive) {
        searchActive = false
        query = ""
    }

    Scaffold(modifier = modifier, topBar = {
        SongAppBar(
            title = "Music Player",
            leftIcon = R.drawable.ic_search,
            rightIcon = R.drawable.ic_settings,
            searchActive = searchActive,
            searchQuery = query,
            onSearchQueryChange = { query = it },
            onSearchOpen = { searchActive = true },
            onSearchClose = {
                searchActive = false
                query = ""
            }
        )
    }, bottomBar = {
        BottomBar(
            modifier = Modifier.fillMaxWidth(),
            destinations = appState.topLevelDestinations,
            onNavigateToDestination = appState::navigateToTopLevelDestination,
            currentDestination = appState.currentDestination
        )

    }) { innerPadding ->
        FloatingPlayerHost(
            modifier = Modifier.padding(top = innerPadding.calculateTopPadding(), bottom = innerPadding.calculateBottomPadding()),
            songs = displayed,
            currentSong = currentSong,
            currentIndex = currentIndex,
            isPlaying = isPlaying,
            onPrevious = onPrevious,
            onNext = onNext,
            // La file de lecture reste la liste complète de l'onglet : on reconvertit
            // l'index (local à la liste filtrée) vers la liste source. Cliquer une chanson
            // = l'user a trouvé son titre → on quitte le mode recherche.
            onSongClick = { index ->
                onPlay(songs, songs.indexOf(displayed[index]))
                searchActive = false
                query = ""
            },
            onPlayPause = onPlayPause,
            sliderValue = sliderValue,
            onSeek = onSliderValueChanged,
            onClose = onClose,
            onToggleFavorite = onToggleFavorite,
            emptyContent = {
                // Liste vide : soit la source est vide (ex. aucun favori), soit la
                // recherche ne retourne rien.
                if (songs.isEmpty()) {
                    emptyContent()
                } else {
                    EmptyState(
                        icon = R.drawable.ic_search,
                        title = "No results",
                        subtitle = "No song matches \"$query\"."
                    )
                }
            }
        )
    }
}
