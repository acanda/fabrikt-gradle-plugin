package ch.acanda.gradle.fabrikt

import ch.acanda.gradle.fabrikt.matchers.shouldContain
import io.kotest.core.spec.style.StringSpec
import org.gradle.testfixtures.ProjectBuilder
import java.io.File

class ConfigurationInitializerTest : StringSpec({

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

        val taskConfig = project.objects.newInstance(GenerateTaskConfiguration::class.java, "Dog")
        val config = extension.getGenerateExtensions().get().single()
        val defaults = extension.getDefaults().get()

        initializeGenerateTaskConfiguration().invoke(taskConfig, config, defaults)

        taskConfig.sourcesPath shouldContain "src/defaults"
    }

    "should apply convention when neither default nor config are set" {
        val project = ProjectBuilder.builder().build()
        val extension = FabriktExtension(project)
        extension.generate("dog") {
            it.apiFile.set(File("src/main/kotlin/Dog.kt"))
            it.basePackage.set("ch.acanda.dog")
        }

        val taskConfig = project.objects.newInstance(GenerateTaskConfiguration::class.java, "Dog")
        val config = extension.getGenerateExtensions().get().single()
        val defaults = extension.getDefaults().get()

        initializeGenerateTaskConfiguration().invoke(taskConfig, config, defaults)

        taskConfig.sourcesPath shouldContain "src/main/kotlin"
    }

    "should apply config when default is not set and config is set" {
        val project = ProjectBuilder.builder().build()
        val extension = FabriktExtension(project)
        extension.generate("dog") {
            it.apiFile.set(File("src/main/kotlin/Dog.kt"))
            it.basePackage.set("ch.acanda.dog")
            it.sourcesPath.set("src/config")
        }

        val taskConfig = project.objects.newInstance(GenerateTaskConfiguration::class.java, "Dog")
        val config = extension.getGenerateExtensions().get().single()
        val defaults = extension.getDefaults().get()

        initializeGenerateTaskConfiguration().invoke(taskConfig, config, defaults)

        taskConfig.sourcesPath shouldContain "src/config"
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

        val taskConfig = project.objects.newInstance(GenerateTaskConfiguration::class.java, "Dog")
        val config = extension.getGenerateExtensions().get().single()
        val defaults = extension.getDefaults().get()

        initializeGenerateTaskConfiguration().invoke(taskConfig, config, defaults)

        taskConfig.sourcesPath shouldContain "src/config"
    }

    "should apply initializer block" {
        val project = ProjectBuilder.builder().build()
        val extension = FabriktExtension(project)
        extension.generate("dog") {
            it.apiFile.set(File("src/main/kotlin/Dog.kt"))
            it.basePackage.set("ch.acanda.dog")
        }

        val taskConfig = project.objects.newInstance(GenerateTaskConfiguration::class.java, "Dog")
        val config = extension.getGenerateExtensions().get().single()
        val defaults = extension.getDefaults().get()

        initializeGenerateTaskConfiguration { skip.set(true) }.invoke(taskConfig, config, defaults)

        taskConfig.skip shouldContain true
    }

})
