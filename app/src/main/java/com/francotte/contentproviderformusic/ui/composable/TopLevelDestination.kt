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
    val titleTextId: Int,
    val route: String
) {
    data object LIBRARY : TopLevelDestination(
        R.drawable.ic_home,
        R.string.library,
        LIBRARY_ROUTE
    )

    data object FAVORITES : TopLevelDestination(
        R.drawable.ic_favorite,
        R.string.favorites,
        FAVORITES_ROUTE
    )

//    data object PLAYLISTS : TopLevelDestination(
//        R.drawable.ic_playlist_play,
//        R.string.playlists,
//        PLAYLISTS_ROUTE
//    )

}

fun NavDestination?.isTopLevelDestinationInHierarchy(destination: TopLevelDestination): Boolean {
    return this?.hierarchy?.any { navDestination ->
        navDestination.route == destination.route
    } ?: false
}
