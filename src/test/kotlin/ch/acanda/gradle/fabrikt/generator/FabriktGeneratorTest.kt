package ch.acanda.gradle.fabrikt.generator

import ch.acanda.gradle.fabrikt.GenerateTaskConfiguration
import ch.acanda.gradle.fabrikt.GenerateTaskDefaults
import ch.acanda.gradle.fabrikt.listFilesRelative
import ch.acanda.gradle.fabrikt.withDefaults
import io.kotest.core.TestConfiguration
import io.kotest.core.spec.style.WordSpec
import io.kotest.engine.spec.tempdir
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.should
import io.kotest.matchers.string.shouldContain
import org.gradle.testfixtures.ProjectBuilder
import java.io.File

class FabriktGeneratorTest : WordSpec({

    "FabriktGenerateTask" should {

        "generate model classes" {
            val outputDir = tempdir("out")
            val project = ProjectBuilder.builder().build()
            val config = GenerateTaskConfiguration("dog", project)
            config.apiFile.set(apiFile())
            config.apiFragments.setFrom(apiFragment())
            config.basePackage.set("dog")
            config.outputDirectory.set(outputDir)

            generate(config.withDefaults(GenerateTaskDefaults(project)))

            val outputs = outputDir.listFilesRelative()
            outputs shouldContain "src/main/kotlin/dog/models/Dog.kt"
        }

        "postprocess model classes" {
            val outputDir = tempdir("out")
            val project = ProjectBuilder.builder().build()
            val config = GenerateTaskConfiguration("dog", project)
            config.apiFile.set(apiFile())
            config.apiFragments.setFrom(apiFragment())
            config.basePackage.set("dog")
            config.outputDirectory.set(outputDir)
            config.model.ignoreUnknownProperties.set(true)

            generate(config.withDefaults(GenerateTaskDefaults(project)))

            outputDir.resolve("src/main/kotlin/dog/models/Dog.kt").readText() shouldContain "JsonIgnoreProperties"
        }

        "generate client classes" {
            val outputDir = tempdir("out")
            val project = ProjectBuilder.builder().build()
            val config = GenerateTaskConfiguration("dog", project)
            config.apiFile.set(apiFile())
            config.apiFragments.setFrom(apiFragment())
            config.basePackage.set("dog")
            config.outputDirectory.set(outputDir)
            config.client.generate.set(true)

            generate(config.withDefaults(GenerateTaskDefaults(project)))

            val outputs = outputDir.listFilesRelative()
            outputs shouldContain "src/main/kotlin/dog/client/DogsClient.kt"
        }

        "generate controller classes" {
            val outputDir = tempdir("out")
            val project = ProjectBuilder.builder().build()
            val config = GenerateTaskConfiguration("dog", project)
            config.apiFile.set(apiFile())
            config.apiFragments.setFrom(apiFragment())
            config.basePackage.set("dog")
            config.outputDirectory.set(outputDir)
            config.controller.generate.set(true)

            generate(config.withDefaults(GenerateTaskDefaults(project)))

            val outputs = outputDir.listFilesRelative()
            outputs shouldContain "src/main/kotlin/dog/controllers/DogsController.kt"
        }

        "skip generating code" {
            val outputDir = tempdir("out")
            val project = ProjectBuilder.builder().build()
            val config = GenerateTaskConfiguration("dog", project)
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

    }

})

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
