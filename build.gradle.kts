import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    kotlin("jvm")
    `java-gradle-plugin`
    id("io.gitlab.arturbosch.detekt") version "1.23.1"
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
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.1")
}

kotlin {
    jvmToolchain(17)
}

detekt {
    buildUponDefaultConfig = true // preconfigure defaults
    config.setFrom("$projectDir/config/detekt.yaml")
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

    withType<Detekt>().configureEach {
        val compilation =
            project.extensions.getByType<KotlinJvmProjectExtension>().target.compilations.getByName("test")
        classpath.setFrom(compilation.output.classesDirs, compilation.compileDependencyFiles)

        reports {
            xml.required.set(false)
            html.required.set(false)
            txt.required.set(false)
            sarif.required.set(false)
            md.required.set(true)
        }
    }
}
