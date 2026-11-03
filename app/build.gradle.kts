plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.nimi.app"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.nimi.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 4
        versionName = "3.0.0-foss"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2026.09.00"))
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.4")

    // MapLibre Native SDK for Android (100% Open-Source Vector Map Engine — No API Keys)
    implementation("org.maplibre.gl:android-sdk:13.6.1")

    debugImplementation("androidx.compose.ui:ui-tooling")
}