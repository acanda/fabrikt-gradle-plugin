package ch.acanda.gradle.fabrikt

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.gradle.internal.classpath.DefaultClassPath
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.gradle.testkit.runner.internal.PluginUnderTestMetadataReading
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.gradle.tooling.internal.consumer.DefaultModelBuilder
import org.gradle.tooling.model.idea.IdeaProject
import java.io.File
import kotlin.reflect.KClass

class GradleTest : StringSpec({

    "`gradle fabriktGenerate` with full configuration should run fabrikt" {
        val projectDir = projectDir("full-configuration")
        val basePackage = "ch.acanda"
        val outputPath = "build/generated/custom"
        val sourcePath = "src/fabrikt/kotlin"
        val resourcePath = "src/fabrikt/res"
        val openapiPath = createSpec(projectDir)
        val fragmentPaths = createSpecFragments(projectDir)
        projectDir.resolve("build.gradle.kts").writeText(
            """
            |plugins {
            |  id("ch.acanda.gradle.fabrikt")
            |}
            |
            |fabrikt {
            |  generate("dog") { 
            |    apiFile = file("$openapiPath")
            |    apiFragments = files(${fragmentPaths.joinToString { "\"$it\"" }})
            |    externalReferenceResolution = aggressive
            |    basePackage = "$basePackage"
            |    outputDirectory = file("$outputPath")
            |    sourcesPath = "$sourcePath"
            |    resourcesPath = "$resourcePath"
            |    validationLibrary = NoValidation
            |    quarkusReflectionConfig = enabled
            |    typeOverrides {
            |      binary = InputStream
            |      byte = String
            |      datetime = Instant
            |    }
            |    client {
            |      generate = enabled
            |      target = OpenFeign
            |      resilience4j = enabled
            |      suspendModifier = enabled
            |      springResponseEntityWrapper = enabled
            |      springCloudOpenFeignStarterAnnotation = enabled
            |      openFeignClientName = "custom-client"
            |    }
            |    controller {
            |      generate = enabled
            |      authentication = enabled
            |      suspendModifier = enabled
            |      completionStage = enabled
            |      target = Ktor
            |    }
            |    model {
            |      generate = enabled
            |      extensibleEnums = enabled
            |      javaSerialization = enabled
            |      quarkusReflection = enabled
            |      micronautIntrospection = enabled
            |      micronautReflection = enabled
            |      includeCompanionObject = enabled
            |      sealedInterfacesForOneOf = enabled
            |      nonNullMapValues = enabled
            |      ignoreUnknownProperties = enabled
            |      suffix = "Dto"
            |      serializationLibrary = Kotlin
            |    }
            |    skip = false
            |  }
            |}
            """.trimMargin()
        )

        val result = runGradle(projectDir)

        result.task(":fabriktGenerate")
            .shouldNotBeNull()
            .outcome shouldBe TaskOutcome.SUCCESS

        val outputs = projectDir.resolve(outputPath).listFilesRelative()

        val basePath = "$sourcePath/${basePackage.packageToPath()}"
        outputs shouldContain "$basePath/models/DogDto.kt"
        outputs shouldContain "$basePath/client/DogClient.kt"
        outputs shouldContain "$basePath/controllers/DogController.kt"
        outputs shouldContain "$resourcePath/reflection-config.json"
    }

    "`gradle fabriktGenerate` with full configuration using defaults should run fabrikt" {
        val projectDir = projectDir("full-configuration-with-defaults")
        val basePackage = "ch.acanda"
        val outputPath = "build/generated/custom"
        val sourcePath = "src/fabrikt/kotlin"
        val resourcePath = "src/fabrikt/res"
        val openapiPath = createSpec(projectDir)
        val fragmentPaths = createSpecFragments(projectDir)
        projectDir.resolve("build.gradle.kts").writeText(
            """
            |plugins {
            |  id("ch.acanda.gradle.fabrikt")
            |}
            |
            |fabrikt {
            |  defaults {
            |    externalReferenceResolution = aggressive
            |    outputDirectory = file("$outputPath")
            |    sourcesPath = "$sourcePath"
            |    resourcesPath = "$resourcePath"
            |    validationLibrary = Jakarta
            |    quarkusReflectionConfig = enabled
            |    typeOverrides {
            |      datetime = Instant
            |      byte = String
            |      binary = String
            |      uri = String
            |      uuid = String
            |      date = String
            |    }
            |    client {
            |      generate = enabled
            |      target = OpenFeign
            |      resilience4j = enabled
            |      suspendModifier = enabled
            |      springResponseEntityWrapper = enabled
            |      springCloudOpenFeignStarterAnnotation = enabled
            |      openFeignClientName = "custom-client"
            |    }
            |    controller {
            |      generate = enabled
            |      authentication = enabled
            |      suspendModifier = enabled
            |      completionStage = enabled
            |      target = Micronaut
            |    }
            |    model {
            |      generate = enabled
            |      extensibleEnums = enabled
            |      javaSerialization = enabled
            |      quarkusReflection = enabled
            |      micronautIntrospection = enabled
            |      micronautReflection = enabled
            |      includeCompanionObject = enabled
            |      sealedInterfacesForOneOf = enabled
            |      nonNullMapValues = enabled
            |      ignoreUnknownProperties = enabled
            |      suffix = "Dto"
            |      serializationLibrary = Kotlin
            |    }
            |  }
            |
            |  generate("dog") { 
            |    apiFile = file("$openapiPath")
            |    apiFragments = files(${fragmentPaths.joinToString { "\"$it\"" }})
            |    basePackage = "$basePackage"
            |    skip = false
            |  }
            |}
            """.trimMargin()
        )

        val result = runGradle(projectDir)

        result.task(":fabriktGenerate")
            .shouldNotBeNull()
            .outcome shouldBe TaskOutcome.SUCCESS

        val outputs = projectDir.resolve(outputPath).listFilesRelative()

        val basePath = "$sourcePath/${basePackage.packageToPath()}"
        outputs shouldContain "$basePath/models/DogDto.kt"
        outputs shouldContain "$basePath/client/DogClient.kt"
        outputs shouldContain "$basePath/controllers/DogController.kt"
        outputs shouldContain "$resourcePath/reflection-config.json"
    }

    "`gradle fabriktGenerate` with minimal configuration should run fabrikt" {
        val projectDir = projectDir("minimal-configuration")
        val basePackage = "ch.acanda"
        val openapiPath = createSpec(projectDir)
        projectDir.resolve("build.gradle.kts").writeText(
            """
            |plugins {
            |    id("ch.acanda.gradle.fabrikt")
            |}
            |
            |fabrikt {
            |  generate("dog") {
            |    apiFile = file("$openapiPath")
            |    basePackage = "$basePackage"
            |  }
            |}
            """.trimMargin()
        )
        val result = runGradle(projectDir)

        result.task(":fabriktGenerate")
            .shouldNotBeNull()
            .outcome shouldBe TaskOutcome.SUCCESS

        val modelsPath = "build/generated/sources/fabrikt/src/main/kotlin/${basePackage.packageToPath()}/models"
        val files = projectDir.resolve(modelsPath).listFilesRelative()
        files shouldContain "Dog.kt"
    }

    "`gradle fabriktGenerate` with multiple kotlin configurations should run fabrikt" {
        val projectDir = projectDir("multi-config")
        val basePackageA = "ch.acanda.a"
        val basePackageB = "ch.acanda.b"
        val openapiPathDog = createSpec(projectDir, "Dog")
        val openapiPathCat = createSpec(projectDir, "Cat")
        val openapiPathMouse = createSpec(projectDir, "Mouse")
        projectDir.resolve("build.gradle.kts").writeText(
            """
            |plugins {
            |    id("ch.acanda.gradle.fabrikt")
            |}
            |
            |fabrikt {
            |  generate("dog") {
            |    apiFile = file("$openapiPathDog")
            |    basePackage = "$basePackageA"
            |  }
            |  generate("cat") {
            |    apiFile = file("$openapiPathCat")
            |    basePackage = "$basePackageB"
            |  }
            |  generate("mouse") {
            |    apiFile = file("$openapiPathMouse")
            |    basePackage = "$basePackageB"
            |  }
            |}
            """.trimMargin()
        )
        val result = runGradle(projectDir)

        result.task(":fabriktGenerate")
            .shouldNotBeNull()
            .outcome shouldBe TaskOutcome.SUCCESS

        val modelsPathA = "build/generated/sources/fabrikt/src/main/kotlin/${basePackageA.packageToPath()}/models"
        val filesA = projectDir.resolve(modelsPathA).listFilesRelative()
        filesA shouldContain "Dog.kt"

        val modelsPathB = "build/generated/sources/fabrikt/src/main/kotlin/${basePackageB.packageToPath()}/models"
        val filesB = projectDir.resolve(modelsPathB).listFilesRelative()
        filesB shouldNotContain "Cat.kt"
        filesB shouldContain "Mouse.kt"
    }

    "`gradle fabriktGenerate` with multiple groovy configurations should run fabrikt" {
        val projectDir = projectDir("groovy-config")
        val basePackage = "ch.acanda"
        val openapiPathDog = createSpec(projectDir, "Dog")
        val openapiPathCat = createSpec(projectDir, "Cat")
        projectDir.resolve("build.gradle").writeText(
            """
            |plugins {
            |    id 'ch.acanda.gradle.fabrikt'
            |}
            |
            |fabrikt {
            |  dog {
            |    apiFile = file('$openapiPathDog')
            |    basePackage = '$basePackage.dog'
            |  }
            |  cat {
            |    apiFile = file('$openapiPathCat')
            |    basePackage = '$basePackage.cat'
            |  }
            |}
            """.trimMargin()
        )
        val result = runGradle(projectDir)

        result.task(":fabriktGenerate")
            .shouldNotBeNull()
            .outcome shouldBe TaskOutcome.SUCCESS

        val modelsPath = "build/generated/sources/fabrikt/src/main/kotlin/${basePackage.packageToPath()}"
        val files = projectDir.resolve(modelsPath).listFilesRelative()
        files shouldContain "dog/models/Dog.kt"
        files shouldContain "cat/models/Cat.kt"
    }

    "`gradle compileKotlin` should run fabrikt" {
        val projectDir = projectDir("compileKotlin")
        val basePackage = "ch.acanda"
        val openapiPath = createSpec(projectDir)
        projectDir.resolve("build.gradle.kts").writeText(
            """
            |plugins {
            |  kotlin("jvm")
            |  id("ch.acanda.gradle.fabrikt")
            |}
            |
            |repositories {
            |  mavenCentral()
            |}
            |
            |dependencies {
            |  implementation("com.fasterxml.jackson.core:jackson-annotations:2.15.2")
            |  implementation("jakarta.validation:jakarta.validation-api:3.0.2")
            |}
            |
            |fabrikt {
            |  generate("dog") {
            |    apiFile = file("$openapiPath")
            |    basePackage = "$basePackage"
            |  }
            |}
            """.trimMargin()
        )
        val result = runGradle(projectDir, "compileKotlin")

        result.task(":fabriktGenerate")
            .shouldNotBeNull()
            .outcome shouldBe TaskOutcome.SUCCESS

        result.task(":compileKotlin")
            .shouldNotBeNull()
            .outcome shouldBe TaskOutcome.SUCCESS

        val modelsPath = "build/classes/kotlin/main/${basePackage.packageToPath()}/models"
        val files = projectDir.resolve(modelsPath).listFilesRelative()
        files shouldContain "Dog.class"
    }

    "should add the output directory as a generated source directory in IntelliJ IDEA" {
        val projectDir = projectDir("idea")
        val basePackage = "ch.acanda"
        val openapiPath = createSpec(projectDir)
        projectDir.resolve("build.gradle.kts").writeText(
            """
            |plugins {
            |  kotlin("jvm")
            |  idea
            |  id("ch.acanda.gradle.fabrikt")
            |}
            |
            |fabrikt {
            |  generate("dog") {
            |    apiFile = file("$openapiPath")
            |    basePackage = "$basePackage"
            |  }
            |}
            """.trimMargin()
        )

        GradleConnector.newConnector()
            .forProjectDirectory(projectDir)
            .useBuildDistribution()
            .connect()
            .use { connection ->
                val ideaProject = connection.getModel(IdeaProject::class)
                val srcDirs = ideaProject.modules
                    .flatMap { module -> module.contentRoots }
                    .flatMap { root -> root.sourceDirectories }
                    .map { srcDir ->
                        val dir = srcDir.directory.relativeTo(projectDir.absoluteFile).path.replace('\\', '/')
                        val gen = if (srcDir.isGenerated) " (gen)" else ""
                        "$dir$gen"
                    }

                srcDirs shouldContain "build/generated/sources/fabrikt/src/main/kotlin (gen)"
            }

    }

}) {

    companion object {

        private fun createSpec(projectDir: File, name: String = "Dog"): String {
            val specPath = "src/main/openapi/$name.yaml"
            val specFile = projectDir.resolve(specPath)
            specFile.parentFile.mkdirs()
            specFile.writeText(
                """
                |openapi: 3.0.3
                |info:
                |  title: Test $name API
                |  version: "1.0"
                |paths:
                |  /${name.lowercase()}:
                |    get:
                |      responses:
                |        200:
                |          description: get $name
                |          content:
                |            application/json:
                |              schema:
                |                ${'$'}ref: "#/components/schemas/$name"
                |
                |components:
                |  schemas: 
                |    $name:
                |      type: object
                |      properties:
                |        isGoodBoy:
                |          type: boolean
                |          default: true
                """.trimMargin()
            )
            return specPath
        }

        private fun createSpecFragments(projectDir: File): List<String> {
            val infoPath = "src/main/openapi/info.yaml"
            val infoFile = projectDir.resolve(infoPath)
            infoFile.parentFile.mkdirs()
            infoFile.writeText(
                """
                |info:
                |  title: The API
                |  version: 1.0.0
                """.trimMargin()
            )

            val securityPath = "src/main/openapi/info.yaml"
            val securityFile = projectDir.resolve(securityPath)
            securityFile.parentFile.mkdirs()
            securityFile.writeText(
                """
                |security:
                |  - basicAuth: []
                """.trimMargin()
            )

            return listOf(securityPath)
        }

        private fun runGradle(
            projectDir: File,
            vararg arguments: String = arrayOf("fabriktGenerate")
        ): BuildResult =
            GradleRunner.create()
                .withProjectDir(projectDir)
                .forwardOutput()
                .withArguments(*(arrayOf("--console=plain", "-i", "--configuration-cache") + arguments))
                .withPluginClasspath()
                .build()

        private fun projectDir(name: String): File {
            val projectDir = File("build/gradle-tests/$name")
            projectDir.deleteRecursivelyExcept(".gradle")
            projectDir.mkdirs()
            projectDir.resolve("settings.gradle.kts").writeText(
                """
                |pluginManagement {
                |  plugins {
                |      kotlin("jvm") version "1.9.23"
                |  }
                |}
                """.trimMargin()
            )
            return projectDir
        }

        private fun File.deleteRecursivelyExcept(path: String) {
            val except = resolve(path)
            listFiles { file -> file != except }?.forEach { it.deleteRecursively() }
        }

        private fun <M : Any> ProjectConnection.getModel(modelClass: KClass<M>): M {
            val classpath = DefaultClassPath.of(PluginUnderTestMetadataReading.readImplementationClasspath())
            return (model(modelClass.java) as DefaultModelBuilder)
                .withInjectedClassPath(classpath)
                .get()
        }

    }

}
