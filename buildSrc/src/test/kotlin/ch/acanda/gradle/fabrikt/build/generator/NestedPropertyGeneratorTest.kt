package ch.acanda.gradle.fabrikt.build.generator

import ch.acanda.gradle.fabrikt.build.ExtensionGeneratorTest.Companion.writeToString
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeSpec
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class NestedPropertyGeneratorTest : StringSpec({

    "nestedProperty(name, class) should create property and syntactic sugar" {
        val typeSpec = TypeSpec.classBuilder("Dog")
            .nestedProperty("owner", ClassName("ch.acanda", "Person"))
            .build()
        typeSpec.writeToString() shouldBe """
            |package ch.acanda
            |
            |import org.gradle.api.Action
            |import org.gradle.api.tasks.Nested
            |
            |public class Dog {
            |  @Nested
            |  public val owner: Person = objects.newInstance(Person::class.java)
            |
            |  public fun owner(action: Action<Person>) {
            |    action.execute(owner)
            |  }
            |}
            |
        """.trimMargin()
    }

})
