pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        // The Kotlin version has to match the one embedded in Gradle.
        // See https://docs.gradle.org/current/userguide/compatibility.html#kotlin.
        kotlin("jvm") version "1.9.0"
    }
}

rootProject.name = "fabrikt-gradle-plugin"
