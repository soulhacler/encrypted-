pluginManagement {
    repositories {
        google()
        mavenCentral()
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

rootProject.name = "Encrypted"

// Pure Kotlin game engine: rules, timer, scoring, puzzle loading. No Android dependencies,
// so it can be unit tested on any JVM.
include(":core")

// The Android app. Pass -PcoreOnly=true to build and test just :core on a machine without the
// Android SDK or Google's Maven repository. That also skips the root build script, which is
// where the Android Gradle plugin is declared.
if (providers.gradleProperty("coreOnly").orNull == "true") {
    rootProject.buildFileName = "core-only.gradle.kts" // intentionally absent
} else {
    include(":app")
}
