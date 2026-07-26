package ch.acanda.gradle.fabrikt

import com.cjbooms.fabrikt.cli.ClientCodeGenTargetType
import com.cjbooms.fabrikt.cli.CodeGenTypeOverride
import com.cjbooms.fabrikt.cli.ControllerCodeGenTargetType
import com.cjbooms.fabrikt.cli.ExternalReferencesResolutionMode
import com.cjbooms.fabrikt.cli.InstantLibrary
import com.cjbooms.fabrikt.cli.JacksonNullabilityMode
import com.cjbooms.fabrikt.cli.SerializationLibrary
import com.cjbooms.fabrikt.cli.ValidationLibrary
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeType
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import java.io.File
import kotlin.reflect.KClass

class FabriktOptionsTest : StringSpec({

    "ExternalReferencesResolution" {
        ExternalReferencesResolutionOption::class shouldMatch ExternalReferencesResolutionMode::class
    }

    "ValidationLibrary" {
        ValidationLibraryOption::class shouldMatch ValidationLibrary::class
    }

    "ClientTarget" {
        ClientTargetOption::class shouldMatch ClientCodeGenTargetType::class
    }

    "ControllerTarget" {
        ControllerTargetOption::class shouldMatch ControllerCodeGenTargetType::class
    }

    "SerializationLibrary" {
        SerializationLibraryOption::class shouldMatch SerializationLibrary::class
    }

    "JacksonNullabilityMode" {
        JacksonNullabilityModeOption::class shouldMatch JacksonNullabilityMode::class
    }

    "InstantLibrary" {
        InstantLibraryOption::class shouldMatch InstantLibrary::class
    }

    "TypeOverrides" {
        val schema: ObjectNode =
            ObjectMapper(YAMLFactory()).registerKotlinModule()
                .readTree(File("src/main/schema/configuration.yaml")) as ObjectNode

        val options = schema.withObject("options")
            .filter { it.get("source").textValue() == "com.cjbooms.fabrikt.cli.CodeGenTypeOverride" }
            .flatMap { option ->
                option.withObject("mapping")
                    .valueStream()
                    .filter { it.nodeType != JsonNodeType.NULL }
                    .map { it.textValue() }
                    .toList()
            }
            .toSet()

        options shouldContainExactlyInAnyOrder CodeGenTypeOverride.entries.map { it.name }.toSet()
    }

}) {

    companion object {
        private infix fun KClass<out FabriktOption>.shouldMatch(that: KClass<out Enum<*>>) {
            withClue({ "${this.simpleName} should have the same number of enum values as ${that.simpleName}." }) {
                this.java.enumConstants.map { it.fabriktOption }.toSet().size shouldBe that.java.enumConstants.size
            }
        }
    }

}
