package com.francotte.contentproviderformusic.ads

import android.app.Activity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

sealed class InterstitialLoadResult {
    data class Success(
        val ad: InterstitialAd,
    ) : InterstitialLoadResult()

    data class Failed(
        val error: LoadAdError,
    ) : InterstitialLoadResult()
}

sealed class InterstitialShowEvent {
    data object Shown : InterstitialShowEvent()

    data class FailedToShow(
        val error: AdError,
    ) : InterstitialShowEvent()

    data object Closed : InterstitialShowEvent()

    data object Clicked : InterstitialShowEvent()
}

@OptIn(ExperimentalCoroutinesApi::class)
suspend fun Activity.loadInterstitialAd(
    adUnitId: String,
    adRequest: AdRequest = AdRequest.Builder().build(),
): InterstitialLoadResult =
    suspendCancellableCoroutine { continuation ->

        InterstitialAd.load(
            this,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    ad.setImmersiveMode(true)
                    if (continuation.isActive) continuation.resume(InterstitialLoadResult.Success(ad))
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    if (continuation.isActive) continuation.resume(InterstitialLoadResult.Failed(error))
                }
            },
        )
    }

fun InterstitialAd.showInterstitialAd(activity: Activity): Flow<InterstitialShowEvent> =
    callbackFlow {
        fullScreenContentCallback =
            object : FullScreenContentCallback() {
                override fun onAdShowedFullScreenContent() {
                    trySend(InterstitialShowEvent.Shown)
                }

                override fun onAdClicked() {
                    trySend(InterstitialShowEvent.Clicked)
                }

                override fun onAdDismissedFullScreenContent() {
                    trySend(InterstitialShowEvent.Closed)
                    close()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    trySend(InterstitialShowEvent.FailedToShow(adError))
                    close()
                }
            }

        show(activity)

        awaitClose {
            fullScreenContentCallback = null
        }
    }
