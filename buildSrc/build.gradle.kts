import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-gradle-plugin`
    idea
    alias(libs.plugins.detekt)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.fabrikt)
    implementation(libs.kotlinpoet)
    implementation(libs.bundles.jackson)
    detektPlugins(libs.detekt.formatting)
}

kotlin {
    jvmToolchain(libs.versions.jvm.get().toInt())
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
            useJUnitJupiter(libs.versions.junit)
            dependencies {
                implementation(libs.kotest.runner.junit5)
                implementation(libs.kotest.property)
                runtimeOnly(libs.junit.platform.launcher)
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
