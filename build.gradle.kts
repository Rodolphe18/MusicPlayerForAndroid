// Top-level build file where you can add configuration options common to all sub-projects/modules.

// Le plugin OSS licenses ne publie pas de "plugin marker" sur le Gradle Plugin Portal :
// il s'applique via le classpath buildscript classique, pas via le plugins DSL.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.google.android.gms:oss-licenses-plugin:0.10.6")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.hilt.plugin) apply false
    alias(libs.plugins.ksp) apply false
}