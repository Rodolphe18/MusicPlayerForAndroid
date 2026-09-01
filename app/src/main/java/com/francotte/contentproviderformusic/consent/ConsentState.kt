package com.francotte.contentproviderformusic.consent

sealed interface ConsentState {
    data object Idle : ConsentState

    data object Loading : ConsentState

    data class Ready(
        val canRequestAds: Boolean,
    ) : ConsentState

    /**
     * Le CMP n'a pas pu être interrogé (réseau, ou aucun message RGPD publié côté AdMob).
     * Distinct de Ready(canRequestAds = false), qui signifie « consentement requis, pas
     * encore donné » et doit au contraire déclencher l'affichage du formulaire.
     */
    data object Failed : ConsentState
}
