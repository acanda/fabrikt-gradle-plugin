package ch.acanda.gradle.fabrikt

import ch.acanda.gradle.fabrikt.matchers.shouldContain
import io.kotest.core.spec.style.StringSpec
import org.gradle.testfixtures.ProjectBuilder
import java.io.File

class FabriktExtensionTest : StringSpec({

    "should apply default when config is not set" {
        val project = ProjectBuilder.builder().build()
        val extension = FabriktExtension(project)
        extension.defaults {
            it.sourcesPath.set("src/defaults")
        }
        extension.generate("dog") {
            it.apiFile.set(File("src/main/kotlin/Dog.kt"))
            it.basePackage.set("ch.acanda.dog")
        }

        val config = extension.getTaskConfigurations().get().single()

        config.sourcesPath shouldContain "src/defaults"
    }

    "should apply convention when neither default nor config are set" {
        val project = ProjectBuilder.builder().build()
        val extension = FabriktExtension(project)
        extension.generate("dog") {
            it.apiFile.set(File("src/main/kotlin/Dog.kt"))
            it.basePackage.set("ch.acanda.dog")
        }

        val config = extension.getTaskConfigurations().get().single()

        config.sourcesPath shouldContain "src/main/kotlin"
    }

    "should apply config when default is not set and config is set" {
        val project = ProjectBuilder.builder().build()
        val extension = FabriktExtension(project)
        extension.generate("dog") {
            it.apiFile.set(File("src/main/kotlin/Dog.kt"))
            it.basePackage.set("ch.acanda.dog")
            it.sourcesPath.set("src/config")
        }

        val config = extension.getTaskConfigurations().get().single()

        config.sourcesPath shouldContain "src/config"
    }

    "should apply config when both default and config are set" {
        val project = ProjectBuilder.builder().build()
        val extension = FabriktExtension(project)
        extension.defaults {
            it.sourcesPath.set("src/defaults")
        }
        extension.generate("dog") {
            it.apiFile.set(File("src/main/kotlin/Dog.kt"))
            it.basePackage.set("ch.acanda.dog")
            it.sourcesPath.set("src/config")
        }

        val config = extension.getTaskConfigurations().get().single()

        config.sourcesPath shouldContain "src/config"
    }

})
