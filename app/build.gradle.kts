plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.example.myapp"
    compileSdk = 30  // Android 11 for compatibility

    defaultConfig {
        applicationId = "com.example.myapp"
        minSdk = 16   // Android 4.1
        targetSdk = 30
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8  // Java 8
        targetCompatibility = JavaVersion.VERSION_1_8  // Java 8
    }
    
    kotlinOptions {
        jvmTarget = "1.8"  // Kotlin also targets Java 8
    }
}

dependencies {
    // NO dependencies - pure Android SDK
}
