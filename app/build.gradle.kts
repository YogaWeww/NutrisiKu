plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.nutrisiku"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.nutrisiku"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
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
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // --- JETPACK COMPOSE ---
    // Dependency utama untuk Jetpack Compose.
    implementation(libs.androidx.activity.compose)
    // Bill of Materials (BOM) untuk memastikan semua library Compose menggunakan versi yang kompatibel.
    implementation(platform(libs.androidx.compose.bom))
    // Library untuk membangun UI.
    implementation(libs.androidx.ui)
    // Library untuk grafis, seperti Color, Shape, dll.
    implementation(libs.androidx.ui.graphics)
    // Library untuk melihat preview Composable di Android Studio.
    implementation(libs.androidx.ui.tooling.preview)
    // Library untuk komponen Material Design 3 (Button, Card, Scaffold, dll).
    implementation(libs.androidx.material3)

    // --- MATERIAL ICONS (FIX) ---
    // Menambahkan dependency untuk ikon-ikon tambahan seperti CalendarToday, dll.
    implementation(libs.androidx.compose.material.icons.extended)

    // --- DEPENDENCIES LAINNYA UNTUK TESTING ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.accompanist.pager)
    implementation(libs.accompanist.pager.indicators)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.datastore.preferences)
}