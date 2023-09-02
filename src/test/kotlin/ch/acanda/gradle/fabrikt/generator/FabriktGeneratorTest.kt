package ch.acanda.gradle.fabrikt.generator

import com.cjbooms.fabrikt.cli.CodeGenerationType
import io.kotest.core.spec.style.WordSpec
import io.kotest.engine.spec.tempdir
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.paths.aFile
import io.kotest.matchers.paths.exist
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import kotlin.io.path.writeText

class FabriktGeneratorTest : WordSpec({

    "FabriktGenerator" should {
        "generate model classes" {
            val apiFile = tempfile("api", ".yaml").toPath().apply {
                writeText(
                    """
                    |openapi: 3.0.3
                    |info:
                    |  title: Dog
                    |  version: 1.0.0
                    |paths: {}
                    |components:
                    |  schemas: 
                    |    Dog:
                    |      type: object
                    |      required: id
                    |      properties:
                    |        id:
                    |          type: string
                    |        name:
                    |          type: string
                    """.trimMargin()
                )
            }
            val outputDir = tempdir("out").toPath()
            val targets = setOf(CodeGenerationType.HTTP_MODELS)

            generate(apiFile, "dog", outputDir, targets)

            val dogModel = outputDir.resolve("src/main/kotlin/dog/models/Dog.kt")
            dogModel should exist()
            dogModel shouldBe aFile()
        }
    }

})
