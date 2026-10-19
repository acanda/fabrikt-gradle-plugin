package ch.acanda.gradle.fabrikt.generator

import ch.acanda.gradle.fabrikt.CustomTypeMappingConfiguration
import ch.acanda.gradle.fabrikt.JsonSchemaConfiguration
import ch.acanda.gradle.fabrikt.OperationIdTransformConfiguration
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.equals.shouldEqual
import io.kotest.matchers.shouldBe
import org.gradle.testfixtures.ProjectBuilder
import java.io.File

class ConvertersTest : WordSpec({

    val objects = ProjectBuilder.builder().build().objects

    "convert OperationIdTransformation" should {
        "convert operation id transform when both regex and replacement are present" {
            val config = objects.newInstance(OperationIdTransformConfiguration::class.java).apply {
                regex.set("regex")
                replacement.set("replacement")
            }
            val result = config.convertOperationIdTransform()
            result shouldEqual "regex:replacement"
        }

        "return null when replacement is not present" {
            val config = objects.newInstance(OperationIdTransformConfiguration::class.java).apply {
                regex.set("regex")
            }
            val result = config.convertOperationIdTransform()
            result shouldBe null
        }

        "return null when regex is not present" {
            val config = objects.newInstance(OperationIdTransformConfiguration::class.java).apply {
                replacement.set("replacement")
            }
            val result = config.convertOperationIdTransform()
            result shouldBe null
        }

        "return null when both regex and replacement are not present" {
            val config = objects.newInstance(OperationIdTransformConfiguration::class.java)
            val result = config.convertOperationIdTransform()
            result shouldBe null
        }
    }

    "convert CustomTypeMapping" should {
        "convert custom type mapping when all properties are present" {
            val config = objects.newInstance(CustomTypeMappingConfiguration::class.java).apply {
                openApiType.set("string")
                format.set("duration")
                kotlinType.set("java.time.Duration")
                kotlinxSerializer.set("com.example.DurationAsIsoStringSerializer")
            }
            val result = config.convertCustomTypeMapping()
            result shouldEqual "string:duration=java.time.Duration;kotlinx=com.example.DurationAsIsoStringSerializer"
        }

        "convert custom type mapping when optional kotlinxSerializer is not present" {
            val config = objects.newInstance(CustomTypeMappingConfiguration::class.java).apply {
                openApiType.set("string")
                format.set("duration")
                kotlinType.set("java.time.Duration")
            }
            val result = config.convertCustomTypeMapping()
            result shouldEqual "string:duration=java.time.Duration"
        }

        "return null when mandatory property openApiType is not present" {
            val config = objects.newInstance(CustomTypeMappingConfiguration::class.java).apply {
                format.set("duration")
                kotlinType.set("java.time.Duration")
            }
            val result = config.convertCustomTypeMapping()
            result shouldBe null
        }

        "return null when mandatory property format is not present" {
            val config = objects.newInstance(CustomTypeMappingConfiguration::class.java).apply {
                openApiType.set("string")
                kotlinType.set("java.time.Duration")
            }
            val result = config.convertCustomTypeMapping()
            result shouldBe null
        }

        "return null when mandatory kotlinType is not present" {
            val config = objects.newInstance(CustomTypeMappingConfiguration::class.java).apply {
                openApiType.set("string")
                format.set("duration")
            }
            val result = config.convertCustomTypeMapping()
            result shouldBe null
        }

    }

    "convert JsonSchema" should {

        "convert schema file with pointer when all properties are present" {
            val schema = objects.fileProperty()
            schema.set(File("src/test/resources/json-schema.json"))
            val config = objects.newInstance(JsonSchemaConfiguration::class.java).apply {
                file.set(schema)
                schemaPointer.set("/spec/schemaObject")
            }
            val result = config.convertJsonSchemaFile()
            result shouldEqual "${schema.asFile.get().absolutePath}#/spec/schemaObject"
        }

        "convert schema file when only file property is present" {
            val schema = objects.fileProperty()
            schema.set(File("src/test/resources/json-schema.json"))
            val config = objects.newInstance(JsonSchemaConfiguration::class.java).apply {
                file.set(schema)
            }
            val result = config.convertJsonSchemaFile()
            result shouldEqual schema.asFile.get().absolutePath
        }

        "return null when file is not present" {
            val config = objects.newInstance(JsonSchemaConfiguration::class.java).apply {
                schemaPointer.set("/spec/schemaObject")
            }
            val result = config.convertJsonSchemaFile()
            result shouldBe null
        }

    }

})
