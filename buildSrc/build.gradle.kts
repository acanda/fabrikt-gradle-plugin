import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    kotlin("jvm")
    `java-gradle-plugin`
    idea
    id("io.gitlab.arturbosch.detekt") version "1.23.7"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.cjbooms:fabrikt:19.1.0")
    implementation("com.squareup:kotlinpoet:1.18.1")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.15.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.7")
}

kotlin {
    jvmToolchain(17)
}

idea {
    module {
        isDownloadSources = true
    }
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom("${project.layout.projectDirectory}/../config/detekt.yaml")
}

testing {
    suites {
        @Suppress("unused")
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter("5.10.0")
            dependencies {
                val kotestVersion = "5.9.1"
                implementation("io.kotest:kotest-runner-junit5:$kotestVersion")
                implementation("io.kotest:kotest-property:$kotestVersion")
                runtimeOnly("org.junit.platform:junit-platform-launcher:1.10.3")
            }
        }
    }
}

tasks {

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
