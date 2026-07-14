package com.francotte.contentproviderformusic.domain

/**
 * Résout un ensemble de clés (titres) vers les éléments correspondants, dans l'ordre
 * d'itération de [titles]. Générique pour rester testable sans dépendances Android.
 */
fun <T> resolveByTitle(titles: Set<String>, items: List<T>, titleOf: (T) -> String): List<T> {
    val byTitle = items.associateBy(titleOf)
    return titles.mapNotNull { byTitle[it] }
}
