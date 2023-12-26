package ch.acanda.gradle.fabrikt.build.generator

import ch.acanda.gradle.fabrikt.build.ExtensionGeneratorTest.Companion.writeToString
import com.squareup.kotlinpoet.TypeSpec
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class BooleanPropertyGeneratorTest : StringSpec({

    "booleanProperty(name) should create property and syntactic sugar" {
        val typeSpec = TypeSpec.classBuilder("Dog")
            .booleanProperty("isGoodBoy")
            .build()
        typeSpec.writeToString() shouldBe """
            |package ch.acanda
            |
            |import kotlin.Boolean
            |import org.gradle.api.provider.Property
            |
            |public class Dog {
            |  public val isGoodBoy: Property<Boolean> = objects.property(Boolean::class.java)
            |}
            |
        """.trimMargin()
    }

})
