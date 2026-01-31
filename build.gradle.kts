import ch.acanda.gradle.fabrikt.build.GeneratePluginClassesTask
import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-gradle-plugin`
    idea
    alias(libs.plugins.detekt)
    alias(libs.plugins.publish)
    signing
}

group = "ch.acanda.gradle"
version = "1.27.2"

val generatedSources: Provider<Directory> = project.layout.buildDirectory.dir("generated/src/main/kotlin")

gradlePlugin {
    website = "https://github.com/acanda/fabrikt-gradle-plugin"
    vcsUrl = "https://github.com/acanda/fabrikt-gradle-plugin.git"
    plugins {
        create("fabriktPlugin") {
            id = "$group.fabrikt"
            implementationClass = "$group.fabrikt.FabriktPlugin"
            displayName = "Fabrikt Gradle Plugin"
            description = "The Fabrikt Gradle Plugin integrates Fabrikt into your Gradle builds. Fabrikt generates" +
                " Kotlin data classes from an OpenAPI 3.0 or 3.1 specification. It supports advanced features, Spring or" +
                " Micronaut controllers, Ktor route handlers, and OkHttp or OpenFeign clients."
            tags = listOf("openapi", "openapi-3.0", "openapi-3.1", "codegen", "kotlin", "fabrikt")
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.fabrikt)
    testImplementation(kotlin("test"))
    detektPlugins(libs.detekt.formatting)
}

kotlin {
    jvmToolchain(libs.versions.jvm.get().toInt())
}

sourceSets {
    main {
        kotlin {
            srcDir(generatedSources)
        }
    }
}

idea {
    module {
        generatedSourceDirs.add(generatedSources.get().asFile)
        isDownloadSources = true
    }
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom("$projectDir/config/detekt.yaml")
}

testing {
    suites {
        @Suppress("unused")
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter(libs.versions.junit)
            dependencies {
                implementation(libs.kotest.runner.junit5)
                implementation(libs.kotest.property)
                runtimeOnly(libs.junit.platform.launcher)
            }
        }
    }
}

signing {
    useGpgCmd()
}

tasks {

    val generatePluginClasses by registering(GeneratePluginClassesTask::class) {
        schema = file("src/main/schema/configuration.yaml")
        outputDirectory = generatedSources
    }

    compileKotlin {
        dependsOn(generatePluginClasses)
    }

    withType<Jar>().configureEach {
        dependsOn(generatePluginClasses)
    }

    wrapper {
        gradleVersion = libs.versions.gradle.get()
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

        doFirst {
            if (Runtime.version().feature() >= 25) {
                logger.error(
                    "[ERROR] Detekt 1.x does not support Java 25. The current runtime version is ${Runtime.version()}."
                )
            }
        }
    }

}
