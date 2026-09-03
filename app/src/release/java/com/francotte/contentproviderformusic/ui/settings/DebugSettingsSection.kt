package com.francotte.contentproviderformusic.ui.settings

import androidx.compose.runtime.Composable

/**
 * Pendant vide de la version debug : aucun outil de test n'entre dans le binaire de
 * production. L'implémentation réelle est dans `app/src/debug/`, source set qui n'est
 * pas compilé pour la variante release.
 *
 * Ne pas supprimer ce fichier : `SettingsDialog` appelle `DebugSettingsSection()` sans
 * condition, la compilation release échouerait sans lui.
 */
@Composable
internal fun DebugSettingsSection() {
}
