plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.baselineprofile)
}

android {
    namespace = "com.francotte.contentproviderformusic.baselineprofile"
    compileSdk = 36

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    defaultConfig {
        // La generation d'un Baseline Profile exige API 28+ : en deca, l'outil ne
        // sait pas extraire le profil ART de l'appareil. Sans rapport avec le
        // minSdk 23 de l'app, qui reste inchange.
        minSdk = 28
        targetSdk = 36
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    targetProjectPath = ":app"
}

baselineProfile {
    // Le profil est genere a la demande, pas a chaque build release : la generation
    // requiert un emulateur roote et prend plusieurs minutes.
    useConnectedDevices = true
}

dependencies {
    // Module com.android.test : le module EST l'APK de test, d'ou implementation
    // plutot qu'androidTestImplementation.
    implementation(libs.androidx.junit)
    implementation(libs.androidx.espresso.core)
    implementation(libs.androidx.uiautomator)
    implementation(libs.androidx.benchmark.macro.junit4)
}
