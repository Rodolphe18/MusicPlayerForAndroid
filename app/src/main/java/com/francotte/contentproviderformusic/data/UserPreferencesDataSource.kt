package com.francotte.contentproviderformusic.data

import android.util.Log
import androidx.datastore.core.DataStore
import com.francotte.contentproviderformusic.UserPreferences
import com.francotte.contentproviderformusic.copy
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class UserPreferencesDataSource @Inject constructor(
    private val userPreferences: DataStore<UserPreferences>,
): UserDataRepository {

    override val userData = userPreferences.data
        .map {
            UserData(favoritesSongs = it.favoriteTitlesMap.keys)
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
}