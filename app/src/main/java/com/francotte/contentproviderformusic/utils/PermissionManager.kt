package com.francotte.contentproviderformusic.utils

import android.Manifest
import android.Manifest.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.os.Build
import androidx.activity.result.ActivityResultLauncher

object PermissionManager  {


    fun requestRuntimePermission(permissions: ActivityResultLauncher<Array<String>>) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            permissions.launch(arrayOf(READ_EXTERNAL_STORAGE))
        } else {
            permissions.launch(arrayOf(Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.POST_NOTIFICATIONS))
        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            permissions.launch(arrayOf(WRITE_EXTERNAL_STORAGE))
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            permissions.launch(arrayOf(FOREGROUND_SERVICE_MEDIA_PLAYBACK))
        }

    }

}