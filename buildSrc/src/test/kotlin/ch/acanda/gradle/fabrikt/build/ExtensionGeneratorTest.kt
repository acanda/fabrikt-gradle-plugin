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
                ClassName("ch.acanda", "FabriktGenerateExtension")
            )
        val extension = typeSpec.writeToString()

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

        "contain the property targets" {
            extension shouldContainOnlyOnce "public val targets: SetProperty<CodeGenerationType>"
        }

        "contain the property httpClientOpts" {
            extension shouldContainOnlyOnce "public val httpClientOpts: SetProperty<ClientCodeGenOptionType>"
        }

        "contain the property httpClientTarget" {
            extension shouldContainOnlyOnce "public val httpClientTarget: Property<ClientCodeGenTargetType>"
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


