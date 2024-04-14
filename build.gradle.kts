import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    kotlin("jvm")
    `java-gradle-plugin`
    idea
    id("io.gitlab.arturbosch.detekt") version "1.23.6"
    id("com.gradle.plugin-publish") version "1.2.1"
    signing
}

group = "ch.acanda.gradle"
version = "0.7.0"

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
                " Kotlin data classes with support for advanced features, Spring or Micronaut controllers, and" +
                " OkHttp or OpenFeign clients."
            tags = listOf("openapi", "openapi-3.0", "codegen", "kotlin", "fabrikt")
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.cjbooms:fabrikt:15.1.0")
    testImplementation(kotlin("test"))
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.6")
}

kotlin {
    jvmToolchain(17)
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
            useJUnitJupiter("5.10.0")
            dependencies {
                val kotestVersion = "5.8.1"
                implementation("io.kotest:kotest-runner-junit5:$kotestVersion")
                implementation("io.kotest:kotest-property:$kotestVersion")
                runtimeOnly("org.junit.platform:junit-platform-launcher:1.10.2")
            }
        }
    }
}

signing {
    useGpgCmd()
}

tasks {

    wrapper {
        gradleVersion = "8.7"
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
