package com.francotte.contentproviderformusic.ui.settings

import androidx.compose.runtime.Composable
import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * Outils de vérification de Crashlytics, ajoutés au bas des réglages.
 *
 * Ce fichier n'appartient qu'à la variante **debug**. La variante release fournit un
 * pendant vide (`app/src/release/.../DebugSettingsSection.kt`), si bien que ce code
 * n'est pas compilé en production — contrairement à un simple `if (BuildConfig.DEBUG)`,
 * qui rend le bloc inatteignable mais en laisse des restes dans le binaire tant que
 * R8 est désactivé.
 *
 * Libellés volontairement en dur : les traduire dans les 14 locales n'aurait aucun
 * sens pour un outil de développement.
 */
@Composable
internal fun DebugSettingsSection() {
    SectionTitle("Debug")

    // Test sans dégât : valide toute la chaîne (clé Firebase, réseau, envoi) et
    // remonte dans l'onglet « Non fatales » sans fermer l'application.
    SettingsRow("Crashlytics : envoyer un rapport de test") {
        FirebaseCrashlytics.getInstance().apply {
            log("Rapport de test envoyé depuis les réglages")
            setCustomKey("origine", "settings_debug")
            recordException(IllegalStateException("Test Crashlytics - non fatal"))
            // Force l'envoi immédiat, sans attendre le prochain démarrage.
            sendUnsentReports()
        }
    }

    // Plantage réel. Crashlytics n'envoie rien sur le moment : le rapport part au
    // démarrage suivant, il faut donc relancer l'application.
    SettingsRow("Crashlytics : provoquer un plantage") {
        FirebaseCrashlytics.getInstance().log("Plantage de test déclenché depuis les réglages")
        throw RuntimeException("Test Crashlytics - plantage volontaire")
    }
}
