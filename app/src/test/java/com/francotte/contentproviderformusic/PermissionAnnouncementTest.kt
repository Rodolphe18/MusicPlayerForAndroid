package com.francotte.contentproviderformusic

import com.francotte.contentproviderformusic.utils.PermissionManager
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Le contrat RequestMultiplePermissions rend la main sans afficher de dialogue
 * quand tout est deja accorde. Se fier a son seul resultat rejouait donc le toast
 * de confirmation a chaque lancement et a chaque rotation.
 */
class PermissionAnnouncementTest {

    @Test
    fun announces_when_permissions_are_freshly_granted() {
        assertTrue(PermissionManager.shouldAnnounceGrant(grantedBefore = false, grantedNow = true))
    }

    @Test
    fun stays_silent_when_permissions_were_already_granted() {
        assertFalse(PermissionManager.shouldAnnounceGrant(grantedBefore = true, grantedNow = true))
    }

    @Test
    fun stays_silent_when_permissions_are_refused() {
        assertFalse(PermissionManager.shouldAnnounceGrant(grantedBefore = false, grantedNow = false))
    }

    @Test
    fun stays_silent_when_permissions_were_revoked_from_settings() {
        assertFalse(PermissionManager.shouldAnnounceGrant(grantedBefore = true, grantedNow = false))
    }
}
