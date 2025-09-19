package ch.acanda.gradle.fabrikt

import com.cjbooms.fabrikt.cli.ClientCodeGenTargetType
import com.cjbooms.fabrikt.cli.ControllerCodeGenTargetType
import com.cjbooms.fabrikt.cli.ExternalReferencesResolutionMode
import com.cjbooms.fabrikt.cli.SerializationLibrary
import com.cjbooms.fabrikt.cli.ValidationLibrary
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
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

}) {

    companion object {
        private infix fun KClass<out FabriktOption>.shouldMatch(that: KClass<out Enum<*>>) {
            withClue({ "${this.simpleName} should have the same number of enum values as ${that.simpleName}." }) {
                this.java.enumConstants.map { it.fabriktOption }.toSet().size shouldBe that.java.enumConstants.size
            }
        }
    }

}
