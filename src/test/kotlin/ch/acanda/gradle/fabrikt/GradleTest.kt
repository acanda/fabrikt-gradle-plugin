package ch.acanda.gradle.fabrikt

import io.kotest.core.spec.style.StringSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.paths.exist
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome

class GradleTest : StringSpec({

    "`gradle fabriktGenerate` should run fabrikt" {
        val projectDir = tempdir("project")
        val openapiPath = "src/main/openapi"
        val basePackage = "ch.acanda"
        val outputPath = "build/generated/fabrikt"
        val openapiDir = projectDir.resolve(openapiPath)
        openapiDir.mkdirs()
        openapiDir.resolve("api.yaml").writeText(
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
            |      properties:
            |        isGoodBoy:
            |          type: boolean
            |          default: true
            """.trimMargin()
        )
        projectDir.resolve("build.gradle.kts").writeText(
            """
            |plugins {
            |    id("ch.acanda.gradle.fabrikt")
            |}
            |
            |fabrikt {
            |  apiFile.set(file("$openapiPath/api.yaml"))
            |  basePackage.set("$basePackage")
            |  outputDirectory.set(file("$outputPath"))
            |}
            """.trimMargin()
        )
        val runner = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("fabriktGenerate")
            .withPluginClasspath()!!

        val result = runner.build()!!

        result.task(":fabriktGenerate")
            .shouldNotBeNull()
            .outcome shouldBe TaskOutcome.SUCCESS

        val modelsPath = "$outputPath/src/main/kotlin/${basePackage.replace('.', '/')}/models"
        projectDir.resolve("$modelsPath/Dog.kt").toPath() should exist()
    }

})
