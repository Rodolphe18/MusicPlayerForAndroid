package com.francotte.contentproviderformusic.baselineprofile

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Les Android Vitals restent muets tant que l'app n'est pas en production : cette
 * mesure locale est le seul moyen de constater le gain. Comparer les deux medianes.
 */
@RunWith(AndroidJUnit4::class)
class StartupBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Before
    fun setUp() {
        MediaFixture.install()
    }

    /** Reference : aucune compilation anticipee. */
    @Test
    fun startupWithoutCompilation() = measureStartup(CompilationMode.None())

    /** Avec le profil embarque. Require echoue si le profil est absent. */
    @Test
    fun startupWithBaselineProfile() =
        measureStartup(CompilationMode.Partial(BaselineProfileMode.Require))

    private fun measureStartup(compilationMode: CompilationMode) =
        benchmarkRule.measureRepeated(
            packageName = TARGET_PACKAGE,
            metrics = listOf(StartupTimingMetric()),
            compilationMode = compilationMode,
            startupMode = StartupMode.COLD,
            iterations = 10,
            setupBlock = {
                MediaFixture.grantPermissions(TARGET_PACKAGE)
                pressHome()
            },
        ) {
            startActivityAndWait()
            device.wait(Until.hasObject(By.res(MINI_PLAYER_TAG)), MINI_PLAYER_TIMEOUT_MS)
        }
}
