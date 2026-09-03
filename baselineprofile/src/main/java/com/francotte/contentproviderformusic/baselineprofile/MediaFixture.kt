package com.francotte.contentproviderformusic.baselineprofile

import android.content.ContentValues
import android.os.Environment
import android.provider.MediaStore
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice

/**
 * L'auto-play exige une bibliotheque non vide : sans morceau indexe, le parcours
 * n'atteint jamais le miniplayer.
 */
object MediaFixture {

    private const val DISPLAY_NAME = "baseline_profile_fixture.mp3"

    /** Insere la fixture dans MediaStore, en remplacant celle d'une execution precedente. */
    fun install() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val resolver = context.contentResolver
        val collection =
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

        resolver.delete(
            collection,
            "${MediaStore.Audio.Media.DISPLAY_NAME} = ?",
            arrayOf(DISPLAY_NAME),
        )

        val pending = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, DISPLAY_NAME)
            put(MediaStore.Audio.Media.TITLE, "Baseline Profile Fixture")
            put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg")
            put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_MUSIC)
            put(MediaStore.Audio.Media.IS_MUSIC, 1)
            put(MediaStore.Audio.Media.IS_PENDING, 1)
        }

        val uri = resolver.insert(collection, pending)
            ?: error("MediaStore a refuse l'insertion de la fixture")

        resolver.openOutputStream(uri)?.use { output ->
            context.assets.open("test.mp3").use { input -> input.copyTo(output) }
        } ?: error("Flux d'ecriture indisponible pour $uri")

        // IS_PENDING a 0 rend le fichier visible aux autres applications.
        resolver.update(
            uri,
            ContentValues().apply { put(MediaStore.Audio.Media.IS_PENDING, 0) },
            null,
            null,
        )
    }

    /**
     * L'app demande ses permissions depuis onCreate : sans octroi prealable, le
     * dialogue systeme bloquerait le parcours.
     */
    fun grantPermissions(packageName: String) {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.executeShellCommand("pm grant $packageName android.permission.READ_MEDIA_AUDIO")
        device.executeShellCommand("pm grant $packageName android.permission.POST_NOTIFICATIONS")
    }
}
