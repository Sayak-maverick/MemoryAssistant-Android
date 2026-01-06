// App-level build file - this configures how YOUR app specifically is built
// This is where we define dependencies (libraries we want to use)

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")  // Apply Google Services plugin for Firebase
    id("com.google.devtools.ksp") version "1.9.20-1.0.14"  // KSP for Room annotation processing
}

android {
    namespace = "com.memoryassistant"  // Your app's unique identifier
    compileSdk = 34  // The Android SDK version we're using to compile

    defaultConfig {
        applicationId = "com.memoryassistant"  // Unique ID for your app on Play Store
        minSdk = 26      // Minimum Android version: Android 8.0 (Oreo)
        targetSdk = 34   // We're targeting the latest Android features
        versionCode = 1  // Internal version number (increment when updating)
        versionName = "1.0"  // User-visible version (like "1.0", "2.0")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        // Release build - optimized version for users
        release {
            isMinifyEnabled = false  // Code optimization (we'll enable later)
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"  // Kotlin compiles to Java 8 bytecode
    }

    buildFeatures {
        compose = true  // Enable Jetpack Compose (modern UI toolkit)
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core Android libraries
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.1")

    // Jetpack Compose - Modern UI toolkit (like React for Android)
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")  // Material Design 3 components

    // Firebase - Backend as a Service (BaaS)
    // BOM (Bill of Materials) - ensures all Firebase libraries use compatible versions
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))

    // Firebase Authentication - handles user login/signup
    implementation("com.google.firebase:firebase-auth-ktx")

    // Firebase Firestore - NoSQL cloud database
    implementation("com.google.firebase:firebase-firestore-ktx")

    // Firebase Storage - stores images and files
    implementation("com.google.firebase:firebase-storage-ktx")

    // Room Database - Local SQLite database (like IndexedDB for web)
    // Room is Android's recommended way to work with SQLite databases
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")  // Kotlin extensions (coroutines support)
    ksp("androidx.room:room-compiler:2.6.1")  // Annotation processor for generating code

    // Gson - JSON library for type converters
    implementation("com.google.code.gson:gson:2.10.1")

    // Testing libraries (we'll use these later)
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.10.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    // Debugging tools
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
