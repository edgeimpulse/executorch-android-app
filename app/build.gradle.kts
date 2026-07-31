plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.executorchdemo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.executorchdemo"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        // ExecuTorch AAR ships arm64-v8a (devices) and x86_64 (emulator) only.
        ndk {
            abiFilters += listOf("arm64-v8a", "x86_64")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
    }

    // Keep the .pte model uncompressed inside the APK.
    androidResources {
        noCompress += "pte"
    }
}

dependencies {
    // ExecuTorch runtime + Java/Kotlin bindings (XNNPACK CPU backend) from Maven Central.
    implementation("org.pytorch:executorch-android:1.0.0")

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
}
