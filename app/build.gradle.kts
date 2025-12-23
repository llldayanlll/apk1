plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.example.myapp"
    compileSdk = 30  // Lower for better compatibility

    defaultConfig {
        applicationId = "com.example.myapp"
        minSdk = 16   // Android 4.1 - works on VERY old phones
        targetSdk = 30
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}

dependencies {
    // NO DEPENDENCIES - Pure Android SDK only
}
