package com.francotte.contentproviderformusic.ui.composable

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.francotte.contentproviderformusic.R
import com.francotte.contentproviderformusic.model.Song
import com.francotte.contentproviderformusic.ui.state.MusicAppState
import com.francotte.contentproviderformusic.ui.theme.Aurora
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    title: String,
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
    onSettingsClick: () -> Unit = {},
    onAddToPlaylist: () -> Unit = {},
    isRepeatOneEnabled: Boolean = false,
    isShuffleEnabled: Boolean = false,
    onToggleRepeatOne: () -> Unit = {},
    onToggleShuffle: () -> Unit = {},
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
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    Scaffold(
        modifier = modifier,
        containerColor = Aurora.CoralBackground,
    ) {  _ ->
        FloatingPlayerHost(
            modifier = Modifier.fillMaxSize(),
            collapsedBottomInset = BottomBarHeight,
            overlayContent = {
                BottomBar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    destinations = appState.topLevelDestinations,
                    onNavigateToDestination = appState::navigateToTopLevelDestination,
                    currentDestination = appState.currentDestination,
                )
            },
            // En-tête (titre gauche + recherche/réglages) placé dans la liste : défile au scroll.
            header = {
                SongAppBar(
                    modifier = Modifier.padding(top = 28.dp),
                    title = title,
                    leftIcon = R.drawable.ic_search,
                    rightIcon = R.drawable.ic_settings,
                    searchActive = searchActive,
                    searchQuery = query,
                    onSearchQueryChange = { query = it },
                    onSearchOpen = { searchActive = true },
                    onSearchClose = {
                        searchActive = false
                        query = ""
                    },
                    actionIconContentDescription = stringResource(R.string.cd_settings),
                    onActionClick = onSettingsClick,
                )
            },
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
            isRepeatOneEnabled = isRepeatOneEnabled,
            isShuffleEnabled = isShuffleEnabled,
            onToggleRepeatOne = onToggleRepeatOne,
            onToggleShuffle = onToggleShuffle,
            onAddToPlaylist = onAddToPlaylist,
            emptyContent = {
                // Liste vide : soit la source est vide (ex. aucun favori), soit la
                // recherche ne retourne rien.
                if (songs.isEmpty()) {
                    emptyContent()
                } else {
                    EmptyState(
                        icon = R.drawable.ic_search,
                        title = stringResource(R.string.search_no_results_title),
                        subtitle = stringResource(R.string.search_no_results_subtitle, query)
                    )
                }
            }
        )
    }
}
