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
version = "1.17.0-SNAPSHOT"

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
                " Kotlin data classes with support for advanced features, Spring or Micronaut controllers, Ktor route" +
                " handlers, and OkHttp or OpenFeign clients."
            tags = listOf("openapi", "openapi-3.0", "codegen", "kotlin", "fabrikt")
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
    }

    register("buildDocs") {
        group = "documentation"
        description = "Build the documentation site using Antora"

        // Disable configuration caching for this task
        notCompatibleWithConfigurationCache("This task uses external processes")

        doLast {
            val isWindows = System.getProperty("os.name").lowercase().contains("windows")
            val nodeInstalled = try {
                val process = if (isWindows) {
                    ProcessBuilder("cmd", "/c", "node", "--version").start()
                } else {
                    ProcessBuilder("node", "--version").start()
                }
                process.waitFor(5, TimeUnit.SECONDS)
                process.exitValue() == 0
            } catch (e: Exception) {
                false
            }

            if (!nodeInstalled) {
                throw GradleException("""
                    Node.js is not installed or not in the PATH. 
                    Please install Node.js and npm from https://nodejs.org/ 
                    and make sure they are available in your PATH.

                    After installing Node.js and npm, you can install Antora with:
                    npm i -g @antora/cli @antora/site-generator
                """.trimIndent())
            }

            // Install Antora CLI and Site Generator locally if not already installed
            try {
                println("Checking if Antora packages are installed...")
                val installCommand = if (isWindows) {
                    arrayOf("cmd", "/c", "npm", "install", "--no-save", "@antora/cli", "@antora/site-generator")
                } else {
                    arrayOf("npm", "install", "--no-save", "@antora/cli", "@antora/site-generator")
                }

                exec {
                    workingDir(projectDir)
                    commandLine(*installCommand)
                }

                println("Running Antora to build documentation...")
                val antoraCommand = if (isWindows) {
                    arrayOf("cmd", "/c", "npx", "@antora/cli", "antora-playbook.yml")
                } else {
                    arrayOf("npx", "@antora/cli", "antora-playbook.yml")
                }

                exec {
                    workingDir(projectDir)
                    commandLine(*antoraCommand)
                }

                println("Documentation built successfully. Open build/site/index.html to view it.")
            } catch (e: Exception) {
                if (e.message?.contains("Cannot run program") == true && 
                    e.message?.contains("npx") == true) {
                    throw GradleException("""
                        The 'npx' command was not found. This usually means npm is not installed correctly.
                        Please ensure npm is installed and available in your PATH.

                        After installing npm, you can install Antora with:
                        npm i -g @antora/cli @antora/site-generator
                    """.trimIndent())
                } else {
                    throw GradleException("""
                        Failed to build documentation: ${e.message}

                        Make sure Node.js and npm are installed and in your PATH.
                        You can install Antora with:
                        npm i -g @antora/cli @antora/site-generator
                    """.trimIndent())
                }
            }
        }
    }

}
