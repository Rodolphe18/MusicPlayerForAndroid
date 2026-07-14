package com.francotte.contentproviderformusic

import com.francotte.contentproviderformusic.domain.resolveByTitle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaylistResolveTest {

    @Test
    fun resolveByTitle_returns_items_in_title_iteration_order() {
        val items = listOf("a", "b", "c")
        val result = resolveByTitle(linkedSetOf("c", "a"), items) { it }
        assertEquals(listOf("c", "a"), result)
    }

    @Test
    fun resolveByTitle_skips_unknown_titles() {
        val result = resolveByTitle(setOf("x"), listOf("a", "b")) { it }
        assertTrue(result.isEmpty())
    }

    @Test
    fun resolveByTitle_empty_titles_returns_empty() {
        val result = resolveByTitle(emptySet(), listOf("a", "b")) { it }
        assertTrue(result.isEmpty())
    }
}
