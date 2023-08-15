plugins {
    kotlin("jvm")
    `java-gradle-plugin`
}

group = "ch.acanda.gradle"
version = "0.1-SNAPSHOT"
val pluginId = "$group.fabrikt"

gradlePlugin {
    plugins {
        create("fabriktPlugin") {
            id = pluginId
            implementationClass = "$pluginId.FabriktPlugin"
            displayName = "Fabrikt Gradle Plugin"
            description = "Generates Kotlin code from an OpenAPI 3 specification."
            tags.set(listOf("openapi", "openapi-3.0", "codegen", "kotlin", "fabrikt"))
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(17)
}

testing {
    suites {
        @Suppress("unused")
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter("5.10.0")
            dependencies {
                val kotestVersion = "5.6.2"
                implementation("io.kotest:kotest-runner-junit5:$kotestVersion")
                implementation("io.kotest:kotest-assertions-core:$kotestVersion")
                runtimeOnly("org.junit.platform:junit-platform-launcher:1.10.0")
            }
        }
    }
}

tasks {
    wrapper {
        gradleVersion = "8.2.1"
    }
}
