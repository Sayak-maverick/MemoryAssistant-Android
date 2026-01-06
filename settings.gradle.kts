// This file tells Gradle which modules are part of your project
// For now, we just have one module: the 'app' module

pluginManagement {
    repositories {
        google()      // Google's Android repository
        mavenCentral() // Central repository for Java/Kotlin libraries
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "MemoryAssistant"
include(":app")  // This includes our main app module
