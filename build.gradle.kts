// Top-level build file where you can add configuration options common to all sub-projects/modules.
// Think of this as the "master settings" for your entire project

plugins {
    // Android Application Plugin - allows us to build Android apps
    id("com.android.application") version "8.2.0" apply false

    // Kotlin Plugin - lets us write code in Kotlin language
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false

    // Google Services Plugin - allows Firebase to work with your app
    // This reads google-services.json and generates the necessary config
    id("com.google.gms.google-services") version "4.4.0" apply false
}
