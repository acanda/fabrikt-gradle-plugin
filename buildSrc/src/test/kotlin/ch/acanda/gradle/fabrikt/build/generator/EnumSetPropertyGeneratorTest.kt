package ch.acanda.gradle.fabrikt.build.generator

import ch.acanda.gradle.fabrikt.build.ExtensionGeneratorTest
import ch.acanda.gradle.fabrikt.build.ExtensionGeneratorTest.Companion.writeToString
import com.squareup.kotlinpoet.TypeSpec
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class EnumSetPropertyGeneratorTest : StringSpec({

    "enumSetProperty(name, type) should create property and syntactic sugar" {
        val typeSpec = TypeSpec.classBuilder("Dog")
            .enumSetProperty("size", ExtensionGeneratorTest.Companion.Size::class)
            .build()
        typeSpec.writeToString() shouldBe """
            |package ch.acanda
            |
            |import ch.acanda.gradle.fabrikt.build.ExtensionGeneratorTest
            |import kotlin.collections.Iterable
            |import org.gradle.api.provider.Provider
            |import org.gradle.api.provider.SetProperty
            |
            |public class Dog {
            |  public val size: SetProperty<ExtensionGeneratorTest.Companion.Size> =
            |      objects.setProperty(ExtensionGeneratorTest.Companion.Size::class.java).convention(null as Set<ExtensionGeneratorTest.Companion.Size>?)
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
            |  public fun size(vararg size: ExtensionGeneratorTest.Companion.Size) {
            |    this.size.set(size.toSet())
            |  }
            |
            |  public fun size(size: Iterable<ExtensionGeneratorTest.Companion.Size>) {
            |    this.size.set(size.toSet())
            |  }
            |
            |  public fun size(size: Provider<out Iterable<ExtensionGeneratorTest.Companion.Size>>) {
            |    this.size.set(size)
            |  }
            |}
            |
        """.trimMargin()
    }

})
