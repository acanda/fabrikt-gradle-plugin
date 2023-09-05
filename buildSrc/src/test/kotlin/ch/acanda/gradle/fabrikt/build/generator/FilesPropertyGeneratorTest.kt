package ch.acanda.gradle.fabrikt.build.generator

import ch.acanda.gradle.fabrikt.build.ExtensionGeneratorTest.Companion.writeToString
import com.squareup.kotlinpoet.TypeSpec
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class FilesPropertyGeneratorTest : StringSpec({

    "filesProperty(name) should create property and syntactic sugar" {
        val typeSpec = TypeSpec.classBuilder("Dog")
            .filesProperty("pictures")
            .build()
        typeSpec.writeToString() shouldBe """
            |package ch.acanda
            |
            |import kotlin.Any
            |import kotlin.collections.Iterable
            |import org.gradle.api.`file`.ConfigurableFileCollection
            |
            |public class Dog {
            |  public val pictures: ConfigurableFileCollection = objects.fileCollection()
            |
            |  public fun pictures(vararg pictures: Any) {
            |    this.pictures.setFrom(*pictures)
            |  }
            |
            |  public fun pictures(pictures: Iterable<Any>) {
            |    this.pictures.setFrom(pictures)
            |  }
            |}
            |
        """.trimMargin()
    }

})
