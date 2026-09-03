package com.francotte.contentproviderformusic.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Parcours profile : lancement a froid, chargement MediaStore, auto-play, miniplayer.
 *
 * Le signal de fin est l'apparition du miniplayer : startActivityAndWait rendrait la
 * main avant l'attachement du MediaController.
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Before
    fun setUp() {
        MediaFixture.install()
        MediaFixture.grantPermissions(TARGET_PACKAGE)
    }

    @Test
    fun startupToAutoPlay() {
        baselineProfileRule.collect(
            packageName = TARGET_PACKAGE,
            includeInStartupProfile = true,
        ) {
            pressHome()
            startActivityAndWait()
            val appeared =
                device.wait(Until.hasObject(By.res(MINI_PLAYER_TAG)), MINI_PLAYER_TIMEOUT_MS)
            check(appeared) {
                "Miniplayer absent apres $MINI_PLAYER_TIMEOUT_MS ms : l'auto-play n'a pas " +
                    "demarre. Verifier l'indexation de la fixture et BENCHMARK_MODE."
            }
        }
    }
}

internal const val TARGET_PACKAGE = "com.francotte.musicplayer"
internal const val MINI_PLAYER_TAG = "miniPlayer"
internal const val MINI_PLAYER_TIMEOUT_MS = 15_000L
