package com.francotte.contentproviderformusic.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat

/**
 * Distingue la permission **indispensable** des permissions de confort.
 *
 * Historiquement les deux étaient demandées en bloc et le moindre refus laissait
 * l'app sur un écran vide : refuser les notifications suffisait à masquer toute la
 * bibliothèque, définitivement. Seul l'accès aux fichiers audio conditionne
 * désormais l'affichage.
 */
object PermissionManager {

    /** Sans elle, il n'y a tout simplement aucune musique à lire. */
    val audioPermission: String
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

    /** Confort : leur refus dégrade l'expérience mais ne doit jamais bloquer l'app. */
    private val optionalPermissions: Array<String>
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            emptyArray()
        }

    fun requestRuntimePermission(permissions: ActivityResultLauncher<Array<String>>) {
        permissions.launch(arrayOf(audioPermission) + optionalPermissions)
    }

    /**
     * État réel côté système. À préférer au résultat du dialogue, qui ne dit rien
     * d'une permission accordée entre-temps depuis les réglages Android.
     */
    fun hasAudioPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, audioPermission) ==
            PackageManager.PERMISSION_GRANTED
}
