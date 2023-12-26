package ch.acanda.gradle.fabrikt.build.generator

import ch.acanda.gradle.fabrikt.build.ExtensionGeneratorTest.Companion.writeToString
import com.squareup.kotlinpoet.TypeSpec
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class FilePropertyGeneratorTest : StringSpec({

    "fileProperty(name) should create property and syntactic sugar" {
        val typeSpec = TypeSpec.classBuilder("Dog")
            .fileProperty("healthRecord")
            .build()
        typeSpec.writeToString() shouldBe """
            |package ch.acanda
            |
            |import org.gradle.api.`file`.RegularFileProperty
            |
            |public class Dog {
            |  public val healthRecord: RegularFileProperty = objects.fileProperty()
            |}
            |
        """.trimMargin()
    }

})
