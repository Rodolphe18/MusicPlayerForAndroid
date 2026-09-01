package com.francotte.contentproviderformusic.consent

import android.app.Activity
import com.google.android.gms.ads.AdRequest
import kotlinx.coroutines.flow.StateFlow

interface ConsentManager {
    /** État courant, observable par l'UI pour ne composer les annonces qu'une fois résolu. */
    val state: StateFlow<ConsentState>

    suspend fun ensureConsent(activity: Activity): Boolean
    fun buildAdRequest(): AdRequest

    /** Vrai si l'utilisateur peut/doit pouvoir rouvrir ses choix de confidentialité (RGPD). */
    fun isPrivacyOptionsRequired(): Boolean

    /** Réaffiche le formulaire UMP pour gérer/révoquer le consentement. */
    suspend fun showPrivacyOptions(activity: Activity)
}
