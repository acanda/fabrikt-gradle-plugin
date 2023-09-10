package ch.acanda.gradle.fabrikt.generator

import ch.acanda.gradle.fabrikt.GenerateTaskConfiguration
import com.cjbooms.fabrikt.cli.CodeGenerationType
import io.kotest.core.spec.style.WordSpec
import io.kotest.engine.spec.tempdir
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.sequences.shouldContain
import io.kotest.matchers.should
import org.gradle.testfixtures.ProjectBuilder
import kotlin.io.path.writeText

class FabriktGeneratorTest : WordSpec({

    "FabriktGenerateTask" should {

        "generate model classes" {
            val apiFile = tempfile("api", ".yaml").toPath().apply {
                writeText(
                    """
                    |openapi: 3.0.3
                    |info:
                    |  title: Dog
                    |  version: 1.0.0
                    |paths:
                    |  /dogs:
                    |    get:
                    |      responses:
                    |        200:
                    |          content:
                    |            application/json:
                    |              schema:
                    |                ${'$'}ref: "#/components/Dog"

                    """.trimMargin()
                )
            }
            val apiFragment = tempfile("fragment", ".yaml").toPath().apply {
                writeText(
                    """
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
            val outputDir = tempdir("out")

            val project = ProjectBuilder.builder().build()
            val config = GenerateTaskConfiguration(project)
            config.apiFile.set(apiFile.toFile())
            config.apiFragments.setFrom(apiFragment)
            config.basePackage.set("dog")
            config.outputDirectory.set(outputDir)
            config.targets.set(setOf(CodeGenerationType.HTTP_MODELS))
            generate(config)

            val outputs = outputDir.walkTopDown()
                .filter { it.isFile }
                .map { it.toRelativeString(outputDir) }
                .sorted()
                .toList()

            outputs shouldContain "src/main/kotlin/dog/models/Dog.kt"
        }

        "generate client classes" {
            val apiFile = tempfile("api", ".yaml").toPath().apply {
                writeText(
                    """
                    |openapi: 3.0.3
                    |info:
                    |  title: Dog
                    |  version: 1.0.0
                    |paths:
                    |  /dogs:
                    |    get:
                    |      responses:
                    |        200:
                    |          content:
                    |            application/json:
                    |              schema:
                    |                ${'$'}ref: "#/components/Dog"

                    """.trimMargin()
                )
            }
            val apiFragment = tempfile("fragment", ".yaml").toPath().apply {
                writeText(
                    """
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
            val outputDir = tempdir("out")

            val project = ProjectBuilder.builder().build()
            val config = GenerateTaskConfiguration(project)
            config.apiFile.set(apiFile.toFile())
            config.apiFragments.setFrom(apiFragment)
            config.basePackage.set("dog")
            config.outputDirectory.set(outputDir)
            config.client.enabled.set(true)
            generate(config)

            val outputs = outputDir.walkTopDown()
                .filter { it.isFile }
                .map { it.toRelativeString(outputDir) }
                .sorted()
                .toList()

            outputs shouldContain "src/main/kotlin/dog/client/DogsClient.kt"
        }

    }

})
