package ch.acanda.gradle.fabrikt.generator

import ch.acanda.gradle.fabrikt.GenerateTaskConfiguration
import ch.acanda.gradle.fabrikt.GenerateTaskDefaults
import ch.acanda.gradle.fabrikt.GenerateTaskExtension
import ch.acanda.gradle.fabrikt.initializeWithDefaults
import ch.acanda.gradle.fabrikt.listFilesRelative
import ch.acanda.gradle.fabrikt.packageToPath
import io.kotest.core.TestConfiguration
import io.kotest.core.spec.style.WordSpec
import io.kotest.engine.spec.tempdir
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.file.exist
import io.kotest.matchers.should
import io.kotest.matchers.shouldNot
import io.kotest.matchers.string.shouldContain
import org.gradle.testfixtures.ProjectBuilder
import java.io.File

class FabriktGeneratorTest : WordSpec({

    "FabriktGenerateTask" should {

        "generate model classes" {
            val outputDir = tempdir("out")
            val config = createConfig()
            config.apiFile.set(apiFile())
            config.apiFragments.setFrom(apiFragment())
            config.basePackage.set("dog")
            config.outputDirectory.set(outputDir)

            generate(config)

            val outputs = outputDir.listFilesRelative()
            outputs shouldContain "src/main/kotlin/dog/models/Dog.kt"
        }

        "postprocess model classes" {
            val outputDir = tempdir("out")
            val config = createConfig()
            config.apiFile.set(apiFile())
            config.apiFragments.setFrom(apiFragment())
            config.basePackage.set("dog")
            config.outputDirectory.set(outputDir)
            config.model.ignoreUnknownProperties.set(true)

            generate(config)

            outputDir.resolve("src/main/kotlin/dog/models/Dog.kt").readText() shouldContain "JsonIgnoreProperties"
        }

        "generate client classes" {
            val outputDir = tempdir("out")
            val config = createConfig()
            config.apiFile.set(apiFile())
            config.apiFragments.setFrom(apiFragment())
            config.basePackage.set("dog")
            config.outputDirectory.set(outputDir)
            config.client.generate.set(true)

            generate(config)

            val outputs = outputDir.listFilesRelative()
            outputs shouldContain "src/main/kotlin/dog/client/DogsClient.kt"
        }

        "generate controller classes" {
            val outputDir = tempdir("out")
            val config = createConfig()
            config.apiFile.set(apiFile())
            config.apiFragments.setFrom(apiFragment())
            config.basePackage.set("dog")
            config.outputDirectory.set(outputDir)
            config.controller.generate.set(true)

            generate(config)

            val outputs = outputDir.listFilesRelative()
            outputs shouldContain "src/main/kotlin/dog/controllers/DogsController.kt"
        }

        "skip generating code" {
            val outputDir = tempdir("out")
            val config = createConfig()
            config.apiFile.set(apiFile())
            config.apiFragments.setFrom(apiFragment())
            config.basePackage.set("dog")
            config.outputDirectory.set(outputDir)
            config.model.generate.set(true)
            config.client.generate.set(true)
            config.controller.generate.set(true)
            config.skip.set(true)

            generate(config)

            val outputs = outputDir.listFilesRelative()
            outputs should beEmpty()
        }

        "clean source and resource dir before generating code" {
            val outputDir = tempdir("out")
            val config = createConfig()
            config.apiFile.set(apiFile())
            config.apiFragments.setFrom(apiFragment())
            config.basePackage.set("dog")
            config.outputDirectory.set(outputDir)

            val packageDir = config.basePackage.get().toString().packageToPath()
            val sourcesDir = outputDir.resolve(config.sourcesPath.get().toString()).resolve(packageDir)
            sourcesDir.mkdirs()
            val sourcesFile = sourcesDir.resolve("src.txt").apply { writeText("Hi!") }
            val resourcesDir = outputDir.resolve(config.resourcesPath.get().toString()).resolve(packageDir)
            resourcesDir.mkdirs()
            val resourcesFile = resourcesDir.resolve("resrc.txt").apply { writeText("Hi!") }

            generate(config)

            val outputs = outputDir.listFilesRelative()
            outputs shouldNot beEmpty()
            sourcesFile shouldNot exist()
            resourcesFile shouldNot exist()
        }

    }

}) {

    companion object {

        fun createConfig(): GenerateTaskConfiguration {
            val project = ProjectBuilder.builder().build()
            val taskConfig = project.objects.newInstance(GenerateTaskConfiguration::class.java, "dog")
            val extConfig = project.objects.newInstance(GenerateTaskExtension::class.java, "dog")
            val defaults = project.objects.newInstance(GenerateTaskDefaults::class.java)
            initializeWithDefaults().invoke(taskConfig, extConfig, defaults)
            return taskConfig
        }

    }

}

fun TestConfiguration.apiFile(): File = tempfile("dog", ".yaml").apply {
    writeText(
        """
        |openapi: 3.0.3
        |info:
        |  title: Dog
        |  version: 1.0.0
        |paths:
        |  /dogs:
        |    get:
        |      responses:
        |        200:
        |          content:
        |            application/json:
        |              schema:
        |                ${'$'}ref: "#/components/Dog"
        |
        """.trimMargin()
    )
}

fun TestConfiguration.apiFragment(): File = tempfile("fragment", ".yaml").apply {
    writeText(
        """
        |components:
        |  schemas: 
        |    Dog:
        |      type: object
        |      required: id
        |      properties:
        |        id:
        |          type: string
        |        name:
        |          type: string
        """.trimMargin()
    )
}
