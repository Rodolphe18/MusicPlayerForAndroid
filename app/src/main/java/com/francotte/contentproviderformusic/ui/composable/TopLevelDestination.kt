package com.francotte.contentproviderformusic.ui.composable

import androidx.annotation.DrawableRes
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import com.francotte.contentproviderformusic.R

const val LIBRARY_ROUTE = "library"
const val FAVORITES_ROUTE = "favorites"
const val PLAYLISTS_ROUTE = "playlists"

sealed class TopLevelDestination(
    @DrawableRes val icon: Int,
    @DrawableRes val selectedIcon: Int,
    val titleTextId: Int,
    val route: String
) {
    data object LIBRARY : TopLevelDestination(
        R.drawable.ic_house,
        R.drawable.ic_house_fill,
        R.string.library,
        LIBRARY_ROUTE
    )

    data object FAVORITES : TopLevelDestination(
        R.drawable.ic_heart,
        R.drawable.ic_heart_fill,
        R.string.favorites,
        FAVORITES_ROUTE
    )

    data object PLAYLISTS : TopLevelDestination(
        R.drawable.ic_playlist,
        R.drawable.ic_playlist_fill,
        R.string.playlists,
        PLAYLISTS_ROUTE
    )

}

fun NavDestination?.isTopLevelDestinationInHierarchy(destination: TopLevelDestination): Boolean {
    return this?.hierarchy?.any { navDestination ->
        navDestination.route == destination.route
    } ?: false
}
