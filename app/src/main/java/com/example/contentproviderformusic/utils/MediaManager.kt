package com.example.contentproviderformusic.utils

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.example.contentproviderformusic.model.Song

object MediaManager {

    fun getUserSongs(context: Context):List<Song> {

        val userSongs = mutableListOf<Song>()

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
        )

        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            MediaStore.Audio.Media.IS_MUSIC + " != 0",
            null,
            null
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val dataColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            val titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
            val durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val data = cursor.getString(dataColumn)
                val title = cursor.getString(titleColumn)
                val artist = cursor.getString(artistColumn)
                val album = cursor.getString(albumColumn)
                val duration = cursor.getLong(durationColumn)
                val uri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                if (duration > 0) {
                    userSongs.add(Song(data, title, artist, album, uri, duration))
                }
            }
        }
        return userSongs.toList()
    }


}