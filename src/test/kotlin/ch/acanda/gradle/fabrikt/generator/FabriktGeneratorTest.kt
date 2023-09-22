package ch.acanda.gradle.fabrikt.generator

import ch.acanda.gradle.fabrikt.GenerateTaskConfiguration
import io.kotest.core.TestConfiguration
import io.kotest.core.spec.style.WordSpec
import io.kotest.engine.spec.tempdir
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.string.shouldContain
import org.gradle.testfixtures.ProjectBuilder
import java.io.File

class FabriktGeneratorTest : WordSpec({

    "FabriktGenerateTask" should {

        "generate model classes" {
            val outputDir = tempdir("out")

            val project = ProjectBuilder.builder().build()
            val config = GenerateTaskConfiguration(project)
            config.apiFile.set(apiFile())
            config.apiFragments.setFrom(apiFragment())
            config.basePackage.set("dog")
            config.outputDirectory.set(outputDir)
            generate(config)

            val outputs = outputDir.walkTopDown()
                .filter { it.isFile }
                .map { it.toRelativeString(outputDir) }
                .sorted()
                .toList()

            outputs shouldContain "src/main/kotlin/dog/models/Dog.kt"
        }

        "postprocess model classes" {
            val outputDir = tempdir("out")

            val project = ProjectBuilder.builder().build()
            val config = GenerateTaskConfiguration(project)
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
            val project = ProjectBuilder.builder().build()
            val config = GenerateTaskConfiguration(project)
            config.apiFile.set(apiFile())
            config.apiFragments.setFrom(apiFragment())
            config.basePackage.set("dog")
            config.outputDirectory.set(outputDir)
            config.client.enabled.set(true)

            generate(config)

            val outputs = outputDir.walkTopDown()
                .filter { it.isFile }
                .map { it.toRelativeString(outputDir) }
                .sorted()
                .toList()

            outputs shouldContain "src/main/kotlin/dog/client/DogsClient.kt"
        }

        "generate controller classes" {
            val outputDir = tempdir("out")
            val project = ProjectBuilder.builder().build()
            val config = GenerateTaskConfiguration(project)
            config.apiFile.set(apiFile())
            config.apiFragments.setFrom(apiFragment())
            config.basePackage.set("dog")
            config.outputDirectory.set(outputDir)
            config.controller.enabled.set(true)

            generate(config)

            val outputs = outputDir.walkTopDown()
                .filter { it.isFile }
                .map { it.toRelativeString(outputDir) }
                .sorted()
                .toList()

            outputs shouldContain "src/main/kotlin/dog/controllers/DogsController.kt"
        }

    }

})

fun TestConfiguration.apiFile(): File = tempfile("api", ".yaml").apply {
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
