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
            |import javax.inject.Inject
            |import kotlin.String
            |import org.gradle.api.Action
            |import org.gradle.api.NamedDomainObjectContainer
            |import org.gradle.api.model.ObjectFactory
            |
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
                ClassName("ch.acanda", "ClientExtension"),
                ClassName("ch.acanda", "ControllerExtension"),
                ClassName("ch.acanda", "ModelExtension")
            )
        val extension = typeSpec.writeToString()

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

        "contain the property typeOverrides" {
            extension shouldContainOnlyOnce "public val typeOverrides: Property<CodeGenTypeOverride>"
        }

        "contain the property typeOverrides" {
            extension shouldContainOnlyOnce "public val validationLibrary: Property<ValidationLibrary>"
        }

        "contain the property typeOverrides" {
            extension shouldContainOnlyOnce "public val quarkusReflectionConfig: Property<Boolean>"
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
            extension shouldContainOnlyOnce "public val target: Property<ClientCodeGenTargetType>"
        }
    }

    "controllerExtension(name)" should {
        val typeSpec = ExtensionGenerator.controllerExtension(
            ClassName("ch.acanda", "ControllerExtension")
        )
        val extension = typeSpec.writeToString()

        "contain the property authentication" {
            extension shouldContainOnlyOnce "public val authentication: Property<Boolean>"
        }

        "contain the property suspendModifier" {
            extension shouldContainOnlyOnce "public val suspendModifier: Property<Boolean>"
        }

        "contain the property target" {
            extension shouldContainOnlyOnce "public val target: Property<ControllerCodeGenTargetType>"
        }
    }

    "modelExtension(name)" should {
        val typeSpec = ExtensionGenerator.modelExtension(
            ClassName("ch.acanda", "ModelExtension")
        )
        val extension = typeSpec.writeToString()

        "contain the property options" {
            extension shouldContainOnlyOnce "public val options: SetProperty<ModelCodeGenOptionType>"
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

        internal enum class Size(val description: String) { SMALL("small"), LARGE("large") }
    }

}
