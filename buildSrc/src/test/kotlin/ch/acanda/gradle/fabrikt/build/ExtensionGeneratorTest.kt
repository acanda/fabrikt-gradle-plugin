package ch.acanda.gradle.fabrikt.build

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContainOnlyOnce

class ExtensionGeneratorTest : WordSpec({

    "fabriktExtension(name, type)" should {
        "create extension" {
            val typeSpec =
                ExtensionGenerator.fabriktExtension(
                    ClassName("ch.acanda", "FabriktExtension"),
                    String::class.asTypeName()
                )
            typeSpec.writeToString() shouldBe """
            |package ch.acanda
            |
            |import javax.`annotation`.processing.Generated
            |import javax.inject.Inject
            |import kotlin.String
            |import org.gradle.api.Action
            |import org.gradle.api.NamedDomainObjectContainer
            |import org.gradle.api.model.ObjectFactory
            |
            |@Generated("ch.acanda.gradle.fabrikt.build.ExtensionGenerator")
            |public open class FabriktExtension @Inject constructor(
            |  private val objects: ObjectFactory,
            |) : NamedDomainObjectContainer<String> by objects.domainObjectContainer(String::class.java) {
            |  public fun generate(name: String, action: Action<String>) {
            |    register(name, action)
            |  }
            |}
            |
            """.trimMargin()
        }
    }

    "fabriktGenerateExtension(name)" should {
        val typeSpec =
            ExtensionGenerator.fabriktGenerateExtension(
                ClassName("ch.acanda", "FabriktGenerateExtension"),
                ClassName("ch.acanda", "TypeOverridesExtension"),
                ClassName("ch.acanda", "ClientExtension"),
                ClassName("ch.acanda", "ControllerExtension"),
                ClassName("ch.acanda", "ModelExtension")
            )
        val extension = typeSpec.writeToString()

        "be annotated with @Generated" {
            extension shouldContainOnlyOnce "@Generated(\"ch.acanda.gradle.fabrikt.build.ExtensionGenerator\")"
        }

        "contain the value enabled" {
            extension shouldContainOnlyOnce "public val enabled: Boolean = true"
        }

        "contain the value disabled" {
            extension shouldContainOnlyOnce "public val disabled: Boolean = false"
        }

        "contain the property apiFile" {
            extension shouldContainOnlyOnce "public val apiFile: RegularFileProperty"
        }

        "contain the property apiFragments" {
            extension shouldContainOnlyOnce "public val apiFragments: ConfigurableFileCollection"
        }

        "contain the property externalReferenceResolution and its values" {
            extension shouldContainOnlyOnce
                "public val externalReferenceResolution: Property<ExternalReferencesResolutionOption>"
            extension shouldContainOnlyOnce "public val targeted: ExternalReferencesResolutionOption"
            extension shouldContainOnlyOnce "public val aggressive: ExternalReferencesResolutionOption"
        }

        "contain the property basePackage" {
            extension shouldContainOnlyOnce "public val basePackage: Property<CharSequence>"
        }

        "contain the property outputDirectory" {
            extension shouldContainOnlyOnce "public val outputDirectory: DirectoryProperty"
        }

        "contain the property sourcesPath" {
            extension shouldContainOnlyOnce "public val sourcesPath: Property<CharSequence>"
        }

        "contain the property resourcesPath" {
            extension shouldContainOnlyOnce "public val resourcesPath: Property<CharSequence>"
        }

        "contain the property validationLibrary" {
            extension shouldContainOnlyOnce "public val validationLibrary: Property<ValidationLibraryOption>"
        }

        "contain the property quarkusReflectionConfig" {
            extension shouldContainOnlyOnce "public val quarkusReflectionConfig: Property<Boolean>"
        }

        "contain the property typeOverrides" {
            extension shouldContainOnlyOnce "public val typeOverrides: TypeOverridesExtension"
        }

        "contain the function typeOverrides(action)" {
            extension shouldContainOnlyOnce "public fun typeOverrides(action: Action<TypeOverridesExtension>)"
        }

        "contain the property client" {
            extension shouldContainOnlyOnce "public val client: ClientExtension"
        }

        "contain the function client(action)" {
            extension shouldContainOnlyOnce "public fun client(action: Action<ClientExtension>)"
        }

        "contain the property controller" {
            extension shouldContainOnlyOnce "public val controller: ControllerExtension"
        }

        "contain the function controller(action)" {
            extension shouldContainOnlyOnce "public fun controller(action: Action<ControllerExtension>)"
        }
    }

    "clientExtension(name)" should {
        val typeSpec = ExtensionGenerator.clientExtension(
            ClassName("ch.acanda", "ClientExtension")
        )
        val extension = typeSpec.writeToString()

        "be annotated with @Generated" {
            extension shouldContainOnlyOnce "@Generated(\"ch.acanda.gradle.fabrikt.build.ExtensionGenerator\")"
        }

        "contain the property enabled" {
            extension shouldContainOnlyOnce "public val enabled: Property<Boolean>"
        }

        "contain the property resilience4j" {
            extension shouldContainOnlyOnce "public val resilience4j: Property<Boolean>"
        }

        "contain the property suspendModifier" {
            extension shouldContainOnlyOnce "public val suspendModifier: Property<Boolean>"
        }

        "contain the property target" {
            extension shouldContainOnlyOnce "public val target: Property<ClientTargetOption>"
        }
    }

    "controllerExtension(name)" should {
        val typeSpec = ExtensionGenerator.controllerExtension(
            ClassName("ch.acanda", "ControllerExtension")
        )
        val extension = typeSpec.writeToString()

        "be annotated with @Generated" {
            extension shouldContainOnlyOnce "@Generated(\"ch.acanda.gradle.fabrikt.build.ExtensionGenerator\")"
        }

        "contain the property authentication" {
            extension shouldContainOnlyOnce "public val authentication: Property<Boolean>"
        }

        "contain the property suspendModifier" {
            extension shouldContainOnlyOnce "public val suspendModifier: Property<Boolean>"
        }

        "contain the property target" {
            extension shouldContainOnlyOnce "public val target: Property<ControllerTargetOption>"
        }
    }

    "modelExtension(name)" should {
        val typeSpec = ExtensionGenerator.modelExtension(
            ClassName("ch.acanda", "ModelExtension")
        )
        val extension = typeSpec.writeToString()

        "be annotated with @Generated" {
            extension shouldContainOnlyOnce "@Generated(\"ch.acanda.gradle.fabrikt.build.ExtensionGenerator\")"
        }

        "contain the property extensibleEnums" {
            extension shouldContainOnlyOnce "public val extensibleEnums: Property<Boolean>"
        }

        "contain the property javaSerialization" {
            extension shouldContainOnlyOnce "public val javaSerialization: Property<Boolean>"
        }

        "contain the property quarkusReflection" {
            extension shouldContainOnlyOnce "public val quarkusReflection: Property<Boolean>"
        }

        "contain the property micronautIntrospection" {
            extension shouldContainOnlyOnce "public val micronautIntrospection: Property<Boolean>"
        }

        "contain the property micronautReflection" {
            extension shouldContainOnlyOnce "public val micronautReflection: Property<Boolean>"
        }

        "contain the property includeCompanionObject" {
            extension shouldContainOnlyOnce "public val includeCompanionObject: Property<Boolean>"
        }
        "contain the property sealedInterfacesForOneOf" {
            extension shouldContainOnlyOnce "public val sealedInterfacesForOneOf: Property<Boolean>"
        }
        "contain the property ignoreUnknownProperties" {
            extension shouldContainOnlyOnce "public val ignoreUnknownProperties: Property<Boolean>"
        }
    }

    "typeOverridesExtension(name)" should {
        val typeSpec = ExtensionGenerator.typeOverridesExtension(
            ClassName("ch.acanda", "TypeOverridesExtension")
        )
        val extension = typeSpec.writeToString()

        "be annotated with @Generated" {
            extension shouldContainOnlyOnce "@Generated(\"ch.acanda.gradle.fabrikt.build.ExtensionGenerator\")"
        }

        "contain the property datetime" {
            extension shouldContainOnlyOnce "public val datetime: Property<DateTimeOverrideOption>"
        }

        "contain the value Instant" {
            extension shouldContainOnlyOnce
                "public val Instant: DateTimeOverrideOption = DateTimeOverrideOption.Instant"
        }

        "contain the value LocalDateTime" {
            extension shouldContainOnlyOnce
                "public val LocalDateTime: DateTimeOverrideOption = DateTimeOverrideOption.LocalDateTime"
        }
    }

}) {

    companion object {
        internal fun TypeSpec.writeToString(): String {
            val fileSpec = FileSpec.builder("ch.acanda", "Dog").addType(this).build()
            val file = StringBuilder()
            fileSpec.writeTo(file)
            return file.toString()
        }
    }

}
