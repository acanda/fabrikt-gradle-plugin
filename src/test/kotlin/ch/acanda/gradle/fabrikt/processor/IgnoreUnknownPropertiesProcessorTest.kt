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
                """openapi: 3.0.3
                |info:
                |  title: The API
                |  version: 1.0.0
                |paths: {}
                |components:
                |  schemas:
                |
                |    Animal:
                |      type: object
                |      discriminator:
                |        propertyName: type
                |        mapping:
                |          Dog: '#/components/schemas/Dog'
                |          Cat: '#/components/schemas/Cat'
                |      required:
                |        - type
                |      properties:
                |        type:
                |          ${'$'}ref: '#/components/schemas/AnimalDiscriminatorType'
                |
                |    AnimalDiscriminatorType:
                |      type: string
                |      enum:
                |        - Dog
                |        - Cat
                |
                |    Dog:
                |      allOf:
                |        - ${'$'}ref: '#/components/schemas/Animal'
                |
                |    Cat:
                |      allOf:
                |        - ${'$'}ref: '#/components/schemas/Animal'
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

        // the base class should not be annotated
        modelsDirectory.resolve("Animal.kt").readText() shouldBe """
            |package a.b.models
            |
            |import com.fasterxml.jackson.`annotation`.JsonSubTypes
            |import com.fasterxml.jackson.`annotation`.JsonTypeInfo
            |
            |@JsonTypeInfo(
            |  use = JsonTypeInfo.Id.NAME,
            |  include = JsonTypeInfo.As.EXISTING_PROPERTY,
            |  property = "type",
            |  visible = true,
            |)
            |@JsonSubTypes(JsonSubTypes.Type(value = Dog::class, name = "Dog"),JsonSubTypes.Type(value =
            |    Cat::class, name = "Cat"))
            |public sealed class Animal() {
            |  public abstract val type: AnimalDiscriminatorType
            |}
            |
        """.trimMargin()

        // only the data classes should be annotated
        modelsDirectory.resolve("Dog.kt").readText() shouldBe """
            |package a.b.models
            |
            |import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
            |import com.fasterxml.jackson.`annotation`.JsonProperty
            |import javax.validation.constraints.NotNull
            |
            |@JsonIgnoreProperties(ignoreUnknown = true)
            |public data class Dog(
            |  @get:JsonProperty("type")
            |  @get:NotNull
            |  @param:JsonProperty("type")
            |  override val type: AnimalDiscriminatorType = AnimalDiscriminatorType.DOG,
            |) : Animal()
            |
        """.trimMargin()
    }

})
