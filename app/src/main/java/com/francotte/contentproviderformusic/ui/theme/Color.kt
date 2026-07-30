package com.francotte.contentproviderformusic.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFFFB4AC)
val PurpleGrey80 = Color(0xFFD8C2C0)
val Pink80 = Color(0xFFFFDAD6)

val Purple40 = Color(0xFFE85D54)
val PurpleGrey40 = Color(0xFF8C5A56)
val Pink40 = Color(0xFF9C423C)

/**
 * Palette "Tropical Fresh" : base sombre bleu-teal + turquoise/lime lumineux + corail/rose.
 * NB : la propriété historique [Purple] est conservée (référencée partout) mais porte
 * désormais l'accent principal corail — c'est la couleur des boutons, onglets actifs, etc.
 */
object Aurora {
    // Base sombre (fond du player, posters typographiques)
    val Night = Color(0xFF0A2A3A)
    // Accent principal (FAB, onglet sélectionné, pastilles, icônes de boutons) : corail poussiéreux
    val Purple = Color(0xFFE85D54)
    // Turquoise (gradients, vignettes) : plus sobre, désaturé
    val Teal = Color(0xFF2E9E8B)
    // Vert d'eau clair : sert de teinte de fond très douce derrière le full player
    val Cyan = Color(0xFF74BEAD)

    // Couleurs d'appoint tropicales, désaturées (tons posés)
    val Lime = Color(0xFFA8BE86)
    val Coral = Color(0xFFE85D54)
    val VividCoral = Color(0xFFFF3B30)
    val CoralBackground = Color(0xFFFFF8F7)
    val Pink = Color(0xFFDDA3B2)

    // Fond du player : base sombre avec une légère transition vers le teal.
    val BarBrush = Brush.linearGradient(listOf(Night, Night, Night, Teal))
    // Vignettes / accents : turquoise -> lime.
    val AccentBrush = Brush.linearGradient(listOf(Teal, Lime))
}
