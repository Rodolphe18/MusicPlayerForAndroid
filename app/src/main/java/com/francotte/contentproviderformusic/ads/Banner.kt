package com.francotte.contentproviderformusic.ads

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.francotte.contentproviderformusic.R
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError


data class BannerConfig(
    val adUnitId: String,
    val adSizes: List<AdSize>,
)


fun AdView.ensureConfig(
    adUnitId: String,
    adSize: AdSize,
) {
    if (this.adUnitId != adUnitId) {
        this.adUnitId = adUnitId
    }
    setAdSize(adSize)
}

fun Context.adaptiveBannerSize(availableWidthPx: Int): AdSize {
    val density = resources.displayMetrics.density
    val adWidthDp = (availableWidthPx / density).toInt()
    return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidthDp)
}

private fun Dp.getAdSizeFromHeight(): AdSize =
    when (this) {
        in 0.dp..55.dp -> AdSize.BANNER
        in 56.dp..80.dp -> AdSize.FULL_BANNER
        100.dp -> AdSize.LARGE_BANNER
        else -> AdSize.BANNER
    }


@Composable
fun Banner(
    useAdaptiveSize: Boolean,
    horizontalPadding: Dp,
    heightFallback: Dp,
) {
    val bannerConfig = BannerConfig(
        adUnitId = "ca-app-pub-8828725570000941/4886746155",
        adSizes = listOf(AdSize.BANNER),
    )
    val context = LocalContext.current
    val config = remember { bannerConfig }

    val density = LocalDensity.current
    val screenWidthPx =
        with(density) {
            LocalConfiguration.current.screenWidthDp.dp
                .roundToPx()
        }
    val paddingPx = with(density) { (horizontalPadding * 2).roundToPx() }
    val availableWidthPx = (screenWidthPx - paddingPx).coerceAtLeast(0)

    val adSize =
        remember(useAdaptiveSize, heightFallback, availableWidthPx) {
            if (useAdaptiveSize) {
                context.adaptiveBannerSize(availableWidthPx)
            } else {
                heightFallback.getAdSizeFromHeight()
            }
        }
    var isLoading by remember(adSize) { mutableStateOf(true) }
    var hasFailed by remember(adSize) { mutableStateOf(false) }
    val bannerHeight = if (useAdaptiveSize) adSize.height.dp else heightFallback

    val adView =
        remember(config.adUnitId, adSize) {
            AdView(context).apply {
                ensureConfig(adUnitId = config.adUnitId, adSize = adSize)

                adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        isLoading = false
                        hasFailed = false
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        isLoading = false
                        hasFailed = true
                    }
                }
            }
        }

    DisposableEffect(adView) {
        adView.loadAd(AdRequest.Builder().build())
        onDispose {
            adView.destroy()
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(bannerHeight),
        contentAlignment = Alignment.Center,
    ) {
        if (isLoading) {
            PlaceHolder(height = bannerHeight)
        }
        if (!hasFailed) {
            AndroidView(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding)
                        .height(bannerHeight),
                factory = { adView },
            )
        }
    }
}

@Composable
private fun PlaceHolder(height: Dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .background(Color.LightGray),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(R.drawable.ic_launcher_foreground),
            contentDescription = null,
            tint = Color.Gray
        )
    }
}


