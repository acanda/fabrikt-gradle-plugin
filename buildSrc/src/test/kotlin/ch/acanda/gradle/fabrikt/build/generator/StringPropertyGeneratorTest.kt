package ch.acanda.gradle.fabrikt.build.generator

import ch.acanda.gradle.fabrikt.build.ExtensionGeneratorTest.Companion.writeToString
import com.squareup.kotlinpoet.TypeSpec
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class StringPropertyGeneratorTest : StringSpec({

    "stringProperty(name) should create property and syntactic sugar" {
        val typeSpec = TypeSpec.classBuilder("Dog")
            .stringProperty("breed")
            .build()
        typeSpec.writeToString() shouldBe """
            |package ch.acanda
            |
            |import kotlin.CharSequence
            |import org.gradle.api.provider.Property
            |import org.gradle.api.provider.Provider
            |
            |public class Dog {
            |  public val breed: Property<CharSequence> = objects.property(CharSequence::class.java)
            |
            |  public fun breed(breed: CharSequence) {
            |    this.breed.set(breed)
            |  }
            |
            |  public fun breed(breed: Provider<out CharSequence>) {
            |    this.breed.set(breed)
            |  }
            |}
            |
        """.trimMargin()
    }

})
