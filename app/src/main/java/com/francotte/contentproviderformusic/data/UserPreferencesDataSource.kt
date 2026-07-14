package com.francotte.contentproviderformusic.data

import android.util.Log
import androidx.datastore.core.DataStore
import com.francotte.contentproviderformusic.UserPreferences
import com.francotte.contentproviderformusic.copy
import com.francotte.contentproviderformusic.model.Playlist
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class UserPreferencesDataSource @Inject constructor(
    private val userPreferences: DataStore<UserPreferences>,
) : UserDataRepository {

    override val userData = userPreferences.data
        .map {
            UserData(
                favoritesSongs = it.favoriteTitlesMap.keys,
                playlists = it.toDomainPlaylists(),
            )
        }

    override suspend fun setFavoritesSongs(songTitle: String, isFavorite: Boolean) {
        try {
            userPreferences.updateData {
                it.copy {
                    if (isFavorite) {
                        favoriteTitles.put(songTitle, true)
                    } else {
                        favoriteTitles.remove(songTitle)
                    }
                }
            }
        } catch (ioException: IOException) {
            Log.e("NiaPreferences", "Failed to update user preferences", ioException)
        }
    }

    override suspend fun createPlaylist(id: Long, title: String, description: String) =
        updatePlaylists { it.withNewPlaylist(id, title, description) }

    override suspend fun deletePlaylist(id: Long) =
        updatePlaylists { it.withoutPlaylist(id) }

    override suspend fun addSongToPlaylist(playlistId: Long, songTitle: String) =
        updatePlaylists { it.withSongAdded(playlistId, songTitle) }

    override suspend fun removeSongFromPlaylist(playlistId: Long, songTitle: String) =
        updatePlaylists { it.withSongRemoved(playlistId, songTitle) }

    /** Lit les playlists (domaine), applique [transform], réécrit toute la liste proto. */
    private suspend fun updatePlaylists(transform: (List<Playlist>) -> List<Playlist>) {
        try {
            userPreferences.updateData { prefs ->
                val newList = transform(prefs.toDomainPlaylists())
                prefs.toBuilder()
                    .clearPlaylists()
                    .addAllPlaylists(newList.map { it.toProto() })
                    .build()
            }
        } catch (ioException: IOException) {
            Log.e("NiaPreferences", "Failed to update playlists", ioException)
        }
    }
}
