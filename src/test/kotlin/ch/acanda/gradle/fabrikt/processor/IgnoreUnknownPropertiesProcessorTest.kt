package ch.acanda.gradle.fabrikt.processor

import com.cjbooms.fabrikt.cli.CodeGen
import com.cjbooms.fabrikt.model.Destinations
import io.kotest.core.spec.style.StringSpec
import io.kotest.engine.spec.tempdir
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.shouldBe
import kotlin.io.path.readText

class IgnoreUnknownPropertiesProcessorTest : StringSpec({

    "should add @JsonIgnoreProperties(ignoreUnknown = true) to data classes" {
        val apiFile = tempfile("api").apply {
            writeText(
                """
                |openapi: 3.0.3
                |info:
                |  title: The API
                |  version: 1.0.0
                |paths: {}
                |components:
                |  schemas: 
                |    dog:
                |      type: object
                |      properties:
                |        isGoodBoy:
                |          type: boolean
                |          default: true
                """.trimMargin()
            )
        }
        val outputDirectory = tempdir("out")
        val basePackage = "a.b"
        CodeGen.main(
            arrayOf(
                "--api-file",
                apiFile.absolutePath,
                "--base-package",
                basePackage,
                "--targets",
                "HTTP_MODELS",
                "--output-directory",
                outputDirectory.absolutePath
            )
        )
        val modelsDirectory = outputDirectory.toPath()
            .resolve(Destinations.MAIN_KT_SOURCE)
            .resolve(Destinations.modelsPackage(basePackage).replace('.', '/'))

        addIgnoreUnknownPropertiesAnnotation(modelsDirectory)

        val modelClass = modelsDirectory.resolve("Dog.kt").readText()
        modelClass shouldBe """
            |package a.b.models
            |
            |import com.fasterxml.jackson.annotation.JsonIgnoreProperties
            |import com.fasterxml.jackson.`annotation`.JsonProperty
            |import javax.validation.constraints.NotNull
            |import kotlin.Boolean
            |
            |@JsonIgnoreProperties(ignoreUnknown = true)
            |public data class Dog(
            |  @param:JsonProperty("isGoodBoy")
            |  @get:JsonProperty("isGoodBoy")
            |  @get:NotNull
            |  public val isGoodBoy: Boolean = true,
            |)
            |
        """.trimMargin()
    }

})
