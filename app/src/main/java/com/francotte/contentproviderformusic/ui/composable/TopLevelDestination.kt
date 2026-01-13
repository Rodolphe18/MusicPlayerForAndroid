package com.francotte.contentproviderformusic.ui.composable

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import com.francotte.contentproviderformusic.R

const val LIBRARY_ROUTE = "library"
const val FAVORITES_ROUTE = "favorites"
const val PLAYLISTS_ROUTE = "playlists"

sealed class TopLevelDestination(
    val icon: ImageVector,
    val titleTextId: Int,
    val route: String
) {
    data object LIBRARY : TopLevelDestination(
        Icons.Filled.Home,
        R.string.library,
        LIBRARY_ROUTE
    )

    data object FAVORITES : TopLevelDestination(
        Icons.Filled.Favorite,
        R.string.favorites,
        FAVORITES_ROUTE
    )

    data object PLAYLISTS : TopLevelDestination(
        Icons.AutoMirrored.Filled.PlaylistPlay,
        R.string.playlists,
        PLAYLISTS_ROUTE
    )

}

fun NavDestination?.isTopLevelDestinationInHierarchy(destination: TopLevelDestination): Boolean {
    return this?.hierarchy?.any { navDestination ->
        navDestination.route == destination.route
    } ?: false
}
