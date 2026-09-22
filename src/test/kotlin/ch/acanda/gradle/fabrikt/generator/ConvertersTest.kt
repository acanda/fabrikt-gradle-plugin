package ch.acanda.gradle.fabrikt.generator

import ch.acanda.gradle.fabrikt.OperationIdTransformConfiguration
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.equals.shouldEqual
import io.kotest.matchers.shouldBe
import org.gradle.testfixtures.ProjectBuilder

class ConvertersTest : WordSpec({

    val objects = ProjectBuilder.builder().build().objects

    "convert OperationIdTransformation" should {
        "convert operation id transform when both regex and replacement are present" {
            val config = objects.newInstance(OperationIdTransformConfiguration::class.java).apply {
                regex.set("regex")
                replacement.set("replacement")
            }
            val result = config.convertOperationIdTransform()
            result shouldEqual "regex:replacement"
        }

        "return null when replacement is not present" {
            val config = objects.newInstance(OperationIdTransformConfiguration::class.java).apply {
                regex.set("regex")
            }
            val result = config.convertOperationIdTransform()
            result shouldBe null
        }

        "return null when regex is not present" {
            val config = objects.newInstance(OperationIdTransformConfiguration::class.java).apply {
                replacement.set("replacement")
            }
            val result = config.convertOperationIdTransform()
            result shouldBe null
        }

        "return null when both regex and replacement are not present" {
            val config = objects.newInstance(OperationIdTransformConfiguration::class.java)
            val result = config.convertOperationIdTransform()
            result shouldBe null
        }
    }

})
