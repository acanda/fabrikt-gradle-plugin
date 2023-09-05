package ch.acanda.gradle.fabrikt.build.generator

import ch.acanda.gradle.fabrikt.build.ExtensionGeneratorTest.Companion.writeToString
import com.squareup.kotlinpoet.TypeSpec
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class NamedTypeGeneratorTest : StringSpec({

    "named() should implement the interface Named" {
        val typeSpec = TypeSpec.classBuilder("Dog").named().build()
        typeSpec.writeToString() shouldBe """
            |package ch.acanda
            |
            |import kotlin.String
            |import org.gradle.api.Named
            |
            |public class Dog : Named {
            |  private val name: String = name
            |
            |  override fun getName(): String = name
            |}
            |
        """.trimMargin()
    }

})
