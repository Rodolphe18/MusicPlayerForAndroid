package com.francotte.contentproviderformusic.ui.playlists

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.francotte.contentproviderformusic.ui.composable.PLAYLISTS_ROUTE


fun NavController.navigateToPlayListsScreen(navOptions: NavOptions? = null) {
    this.navigate(PLAYLISTS_ROUTE, navOptions)
}