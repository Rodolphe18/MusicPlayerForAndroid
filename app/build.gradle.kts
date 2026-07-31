import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt.plugin)
    alias(libs.plugins.ksp)
    alias(libs.plugins.protobuf)
    id("com.google.android.gms.oss-licenses-plugin")
}

// Secrets de signature : jamais versionnés (voir keystore.properties.template).
// Absent = build release non signé, ce qui reste utilisable en local.
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { load(it) }
    }
}

android {
    // Le namespace reste l'ancien : c'est la racine des packages Kotlin, la changer
    // imposerait un refactor complet sans bénéfice. Seul applicationId est public.
    namespace = "com.francotte.contentproviderformusic"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.francotte.musicplayer"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (keystorePropertiesFile.exists()) {
            create("release") {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            if (keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {

        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.windowSizeClass)
    implementation(libs.androidx.compose.material3.adaptive)
    implementation(libs.androidx.compose.material3.navigationSuite)

    implementation(libs.androidx.navigation.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.coil)
    implementation(libs.coil.kt.compose)
    implementation(libs.coil.video)
    implementation(libs.coil.base)
    implementation(libs.coil.compose.base)
    implementation(libs.coil.kt.svg)
    implementation(libs.androidx.media)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.android)
    implementation(libs.hilt.core)
    implementation(libs.hilt.plugin)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.core.splashscreen)

    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.datasource.okhttp)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.session)
    implementation(libs.kotlinx.coroutines.guava)

    implementation(libs.androidx.dataStore)
    implementation(libs.protobuf.kotlin.lite)
    implementation(libs.kotlinx.collections.immutable)

    implementation(libs.play.services.ads)
    implementation(libs.play.services.ads.lite)
    implementation(libs.play.services.ads.identifier)
    implementation(libs.play.services.oss.licenses)
    implementation(libs.androidx.appcompat)
}

// En module unique, KSP (Hilt) ne voit pas les classes proto générées, d'où
// les erreurs "error.NonExistentClass" sur UserPreferences. On ajoute donc
// explicitement les dossiers proto générés aux sources de la tâche KSP.
androidComponents {
    onVariants(selector().all()) { variant ->
        afterEvaluate {
            val capName = variant.name.replaceFirstChar { it.uppercase() }
            tasks.named("ksp${capName}Kotlin") {
                dependsOn("generate${capName}Proto")
                (this as org.jetbrains.kotlin.gradle.tasks.AbstractKotlinCompileTool<*>).source(
                    "build/generated/source/proto/${variant.name}/java",
                    "build/generated/source/proto/${variant.name}/kotlin",
                )
            }
        }
    }
}

protobuf {
    protoc {
        artifact =
            libs.protobuf.protoc
                .get()
                .toString()
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                register("java") {
                    option("lite")
                }
                register("kotlin") {
                    option("lite")
                }
            }
        }
    }
}