plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace   = "com.martin.storage"
    compileSdk  = 37

    defaultConfig {
        applicationId  = "com.martin.storage"
        minSdk         = 26
        targetSdk      = 37
        versionCode    = 1
        versionName    = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled   = true
            isShrinkResources = true
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

    buildFeatures {
        compose = true
    }
}

dependencies {
    // ── Core ──────────────────────────────────────────────────────────────────
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)

    // ── Compose BOM (aligns all Compose library versions) ────────────────────
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.foundation)

    // ── Navigation ────────────────────────────────────────────────────────────
    implementation(libs.androidx.navigation.compose)

    // ── DataStore ─────────────────────────────────────────────────────────────
    implementation(libs.androidx.datastore.preferences)

    // ── Kotlin Serialization ──────────────────────────────────────────────────
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    // ── WorkManager ───────────────────────────────────────────────────────────
    implementation(libs.androidx.work.runtime.ktx)

    // ── CameraX ───────────────────────────────────────────────────────────────
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // ── ML Kit OCR ────────────────────────────────────────────────────────────
    implementation(libs.mlkit.text.recognition)

    // ── Gson ──────────────────────────────────────────────────────────────────
    implementation(libs.gson)

    // ── Debug tooling ─────────────────────────────────────────────────────────
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
