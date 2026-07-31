package com.francotte.contentproviderformusic.utils

import android.Manifest
import android.os.Build
import androidx.activity.result.ActivityResultLauncher

object PermissionManager {

    fun requestRuntimePermission(permissions: ActivityResultLauncher<Array<String>>) {
        val required = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.POST_NOTIFICATIONS,
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        permissions.launch(required)
    }
}
