package ch.acanda.gradle.fabrikt.build.generator

import ch.acanda.gradle.fabrikt.build.ExtensionGeneratorTest.Companion.writeToString
import com.squareup.kotlinpoet.TypeSpec
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class EnumPropertyGeneratorTest : StringSpec({

    "enumProperty(name, type) should create property and syntactic sugar" {
        val typeSpec = TypeSpec.classBuilder("Client")
            .enumProperty("target", ClientTargetOption::class)
            .build()
        typeSpec.writeToString() shouldBe """
            |package ch.acanda
            |
            |import ch.acanda.gradle.fabrikt.ClientTargetOption
            |import org.gradle.api.provider.Property
            |
            |public class Client {
            |  public val target: Property<ClientTargetOption> = objects.property(ClientTargetOption::class.java)
            |
            |  /**
            |   * Generate OkHttp client.
            |   */
            |  public val OkHttp: ClientTargetOption = ClientTargetOption.OkHttp
            |
            |  /**
            |   * Generate OpenFeign client.
            |   */
            |  public val OpenFeign: ClientTargetOption = ClientTargetOption.OpenFeign
            |}
            |
        """.trimMargin()
    }

})
