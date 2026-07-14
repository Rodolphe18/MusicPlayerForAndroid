package com.francotte.contentproviderformusic.data

import com.francotte.contentproviderformusic.UserPreferences
import com.francotte.contentproviderformusic.model.Playlist
import com.francotte.contentproviderformusic.Playlist as ProtoPlaylist

/** proto -> domaine */
fun UserPreferences.toDomainPlaylists(): List<Playlist> =
    playlistsList.map { proto ->
        Playlist(
            id = proto.id,
            title = proto.title,
            description = proto.description,
            songTitles = proto.songTitlesList.toSet(),
        )
    }

/** domaine -> proto */
fun Playlist.toProto(): ProtoPlaylist =
    ProtoPlaylist.newBuilder()
        .setId(id)
        .setTitle(title)
        .setDescription(description)
        .addAllSongTitles(songTitles)
        .build()

/** Transformations pures de la liste de playlists (domaine). */
fun List<Playlist>.withNewPlaylist(id: Long, title: String, description: String): List<Playlist> =
    this + Playlist(id = id, title = title, description = description, songTitles = emptySet())

fun List<Playlist>.withoutPlaylist(id: Long): List<Playlist> =
    filterNot { it.id == id }

fun List<Playlist>.withSongAdded(playlistId: Long, songTitle: String): List<Playlist> =
    map { if (it.id == playlistId) it.copy(songTitles = it.songTitles + songTitle) else it }

fun List<Playlist>.withSongRemoved(playlistId: Long, songTitle: String): List<Playlist> =
    map { if (it.id == playlistId) it.copy(songTitles = it.songTitles - songTitle) else it }
