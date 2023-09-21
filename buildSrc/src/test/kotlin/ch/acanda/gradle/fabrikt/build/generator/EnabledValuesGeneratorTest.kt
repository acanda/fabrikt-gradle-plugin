package ch.acanda.gradle.fabrikt.build.generator

import ch.acanda.gradle.fabrikt.build.ExtensionGeneratorTest.Companion.writeToString
import com.squareup.kotlinpoet.TypeSpec
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class EnabledValuesGeneratorTest : StringSpec({

    "enabledValues() should create the properties enabled and disabled" {
        val typeSpec = TypeSpec.classBuilder("Dog")
            .enabledValues()
            .build()
        typeSpec.writeToString() shouldBe """
            |package ch.acanda
            |
            |import kotlin.Boolean
            |
            |public class Dog {
            |  public val enabled: Boolean = true
            |
            |  public val disabled: Boolean = false
            |}
            |
        """.trimMargin()
    }

})
