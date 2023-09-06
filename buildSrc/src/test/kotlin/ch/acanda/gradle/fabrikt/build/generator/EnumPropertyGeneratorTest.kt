package ch.acanda.gradle.fabrikt.build.generator

import ch.acanda.gradle.fabrikt.build.ExtensionGeneratorTest
import ch.acanda.gradle.fabrikt.build.ExtensionGeneratorTest.Companion.writeToString
import com.squareup.kotlinpoet.TypeSpec
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class EnumPropertyGeneratorTest : StringSpec({

    "enumProperty(name, type) should create property and syntactic sugar" {
        val typeSpec = TypeSpec.classBuilder("Dog")
            .enumProperty("size", ExtensionGeneratorTest.Companion.Size::class)
            .build()
        typeSpec.writeToString() shouldBe """
            |package ch.acanda
            |
            |import ch.acanda.gradle.fabrikt.build.ExtensionGeneratorTest
            |import org.gradle.api.provider.Property
            |import org.gradle.api.provider.Provider
            |
            |public class Dog {
            |  public val size: Property<ExtensionGeneratorTest.Companion.Size> =
            |      objects.property(ExtensionGeneratorTest.Companion.Size::class.java)
            |
            |  /**
            |   * small
            |   */
            |  public val SMALL: ExtensionGeneratorTest.Companion.Size =
            |      ExtensionGeneratorTest.Companion.Size.SMALL
            |
            |  /**
            |   * large
            |   */
            |  public val LARGE: ExtensionGeneratorTest.Companion.Size =
            |      ExtensionGeneratorTest.Companion.Size.LARGE
            |
            |  public fun size(size: ExtensionGeneratorTest.Companion.Size) {
            |    this.size.set(size)
            |  }
            |
            |  public fun size(size: Provider<ExtensionGeneratorTest.Companion.Size>) {
            |    this.size.set(size)
            |  }
            |}
            |
        """.trimMargin()
    }

})
