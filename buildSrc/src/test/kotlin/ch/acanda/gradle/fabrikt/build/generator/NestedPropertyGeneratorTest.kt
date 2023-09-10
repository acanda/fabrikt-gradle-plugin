package ch.acanda.gradle.fabrikt.build.generator

import ch.acanda.gradle.fabrikt.build.ExtensionGeneratorTest.Companion.writeToString
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.gradle.api.provider.Property

class NestedPropertyGeneratorTest : StringSpec({

    "nestedProperty(name, class) should create property and syntactic sugar" {
        val typeSpec = TypeSpec.classBuilder("Dog")
            .nestedProperty("owner", Person::class.asClassName())
            .build()
        typeSpec.writeToString() shouldBe """
            |package ch.acanda
            |
            |import ch.acanda.gradle.fabrikt.build.generator.NestedPropertyGeneratorTest
            |import org.gradle.api.Action
            |import org.gradle.api.tasks.Nested
            |
            |public class Dog {
            |  @Nested
            |  public val owner: NestedPropertyGeneratorTest.Companion.Person =
            |      objects.newInstance(NestedPropertyGeneratorTest.Companion.Person::class.java)
            |
            |  public fun owner(action: Action<NestedPropertyGeneratorTest.Companion.Person>) {
            |    action.execute(owner)
            |  }
            |}
            |
        """.trimMargin()
    }

}) {
    companion object {
        abstract class Person {
            abstract val name: Property<String>
        }
    }
}
