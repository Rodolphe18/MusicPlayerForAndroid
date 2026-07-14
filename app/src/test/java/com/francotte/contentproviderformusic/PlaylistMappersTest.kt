package com.francotte.contentproviderformusic

import com.francotte.contentproviderformusic.data.toDomainPlaylists
import com.francotte.contentproviderformusic.data.toProto
import com.francotte.contentproviderformusic.data.withNewPlaylist
import com.francotte.contentproviderformusic.data.withSongAdded
import com.francotte.contentproviderformusic.data.withSongRemoved
import com.francotte.contentproviderformusic.data.withoutPlaylist
import com.francotte.contentproviderformusic.model.Playlist
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaylistMappersTest {

    @Test
    fun withNewPlaylist_appends_empty_playlist() {
        val result = emptyList<Playlist>().withNewPlaylist(1L, "Road", "trip")
        assertEquals(1, result.size)
        assertEquals(Playlist(1L, "Road", "trip", emptySet()), result[0])
    }

    @Test
    fun withSongAdded_adds_title_to_matching_playlist_only() {
        val start = listOf(
            Playlist(1L, "A", "", emptySet()),
            Playlist(2L, "B", "", emptySet()),
        )
        val result = start.withSongAdded(1L, "Song1")
        assertEquals(setOf("Song1"), result.first { it.id == 1L }.songTitles)
        assertTrue(result.first { it.id == 2L }.songTitles.isEmpty())
    }

    @Test
    fun withSongAdded_is_idempotent() {
        val start = listOf(Playlist(1L, "A", "", setOf("Song1")))
        val result = start.withSongAdded(1L, "Song1")
        assertEquals(setOf("Song1"), result[0].songTitles)
    }

    @Test
    fun withSongRemoved_removes_title() {
        val start = listOf(Playlist(1L, "A", "", setOf("Song1", "Song2")))
        val result = start.withSongRemoved(1L, "Song1")
        assertEquals(setOf("Song2"), result[0].songTitles)
    }

    @Test
    fun withoutPlaylist_drops_by_id() {
        val start = listOf(
            Playlist(1L, "A", "", emptySet()),
            Playlist(2L, "B", "", emptySet()),
        )
        val result = start.withoutPlaylist(1L)
        assertEquals(listOf(2L), result.map { it.id })
    }

    @Test
    fun proto_domain_roundtrip_preserves_data_and_order() {
        val domain = listOf(
            Playlist(10L, "A", "desc", linkedSetOf("s1", "s2")),
            Playlist(20L, "B", "", emptySet()),
        )
        val proto = UserPreferences.newBuilder()
            .addAllPlaylists(domain.map { it.toProto() })
            .build()
        assertEquals(domain, proto.toDomainPlaylists())
    }
}
