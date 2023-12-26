package ch.acanda.gradle.fabrikt

import ch.acanda.gradle.fabrikt.matchers.shouldContain
import ch.acanda.gradle.fabrikt.matchers.shouldContainString
import com.cjbooms.fabrikt.cli.ClientCodeGenTargetType
import com.cjbooms.fabrikt.cli.CodeGenTypeOverride
import com.cjbooms.fabrikt.cli.ControllerCodeGenTargetType
import com.cjbooms.fabrikt.cli.ValidationLibrary
import io.kotest.core.spec.style.WordSpec
import io.kotest.engine.spec.tempdir
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.gradle.testfixtures.ProjectBuilder

/**
 * Tests that the FabriktPlugin:
 * - creates its extensions
 * - registers the task "fabriktGenerate"
 *     - sets all its properties correctly
 *     - uses the proper defaults where the properties are not set
 *
 * It does not test the output of the task "fabriktGenerate".
 */
class FabriktPluginTest : WordSpec({

    "The fabrikt plugin" should {
        "register the task fabriktGenerate with a full configuration" {
            val project = ProjectBuilder.builder().build()
            val apiFile = tempfile("apiSpec", ".yaml")
            val apiFragment = tempfile("apiFragment", ".yaml")
            val basePackage = "ch.acanda"
            val outDir = tempdir("out")
            val srcDir = "src/fabrikt/kotlin"
            val resDir = "src/fabrikt/res"

            project.pluginManager.apply("ch.acanda.gradle.fabrikt")
            project.extensions.configure(FabriktExtension::class.java) { ext ->
                ext.generate("api") {
                    it.apiFile.set(apiFile)
                    it.apiFragments.setFrom(apiFragment)
                    it.basePackage.set(basePackage)
                    it.outputDirectory.set(outDir)
                    it.sourcesPath.set(srcDir)
                    it.resourcesPath.set(resDir)
                    it.typeOverrides(it.DATETIME_AS_INSTANT)
                    it.validationLibrary(it.JAVAX_VALIDATION)
                    it.quarkusReflectionConfig.set(it.enabled)
                    with(it.client) {
                        enabled.set(true)
                        target(OPEN_FEIGN)
                        resilience4j.set(it.enabled)
                        suspendModifier.set(it.enabled)
                    }
                    with(it.controller) {
                        enabled.set(true)
                        target(MICRONAUT)
                        authentication.set(it.enabled)
                        suspendModifier.set(it.enabled)
                    }
                    with(it.model) {
                        enabled.set(false)
                        extensibleEnums.set(it.enabled)
                        javaSerialization.set(it.enabled)
                        quarkusReflection.set(it.enabled)
                        micronautIntrospection.set(it.enabled)
                        micronautReflection.set(it.enabled)
                        includeCompanionObject.set(it.enabled)
                        sealedInterfacesForOneOf.set(it.enabled)
                        ignoreUnknownProperties.set(it.enabled)
                    }
                }
            }

            project.tasks.findByName("fabriktGenerate")
                .shouldNotBeNull()
                .shouldBeInstanceOf<FabriktGenerateTask>()
                .configurations.get().shouldHaveSize(1)
                .first().run {
                    this.apiFile shouldContain apiFile
                    this.apiFragments.files shouldContainExactly listOf(apiFragment)
                    this.basePackage shouldContainString basePackage
                    this.outputDirectory shouldContain outDir
                    this.sourcesPath shouldContainString srcDir
                    this.resourcesPath shouldContainString resDir
                    this.typeOverrides shouldContain CodeGenTypeOverride.DATETIME_AS_INSTANT
                    this.validationLibrary shouldContain ValidationLibrary.JAVAX_VALIDATION
                    this.quarkusReflectionConfig shouldContain true
                    with(client) {
                        enabled shouldContain true
                        resilience4j shouldContain true
                        suspendModifier shouldContain true
                        target shouldContain ClientCodeGenTargetType.OPEN_FEIGN
                    }
                    with(controller) {
                        enabled shouldContain true
                        authentication shouldContain true
                        suspendModifier shouldContain true
                        target shouldContain ControllerCodeGenTargetType.MICRONAUT
                    }
                    with(model) {
                        enabled shouldContain false
                        extensibleEnums shouldContain true
                        javaSerialization shouldContain true
                        quarkusReflection shouldContain true
                        micronautIntrospection shouldContain true
                        micronautReflection shouldContain true
                        includeCompanionObject shouldContain true
                        sealedInterfacesForOneOf shouldContain true
                        ignoreUnknownProperties shouldContain true
                    }
                }
        }

        "register the task fabriktGenerate with a minimal configuration" {
            val project = ProjectBuilder.builder().build()
            val apiFile = tempfile("apiSpec", ".yaml")
            val basePackage = "ch.acanda"
            val outDir = tempdir("out")

            project.pluginManager.apply("ch.acanda.gradle.fabrikt")
            project.extensions.configure(FabriktExtension::class.java) { ext ->
                ext.generate("api") {
                    it.apiFile.set(apiFile)
                    it.basePackage.set(basePackage)
                    it.outputDirectory.set(outDir)
                }
            }

            project.tasks.findByName("fabriktGenerate")
                .shouldNotBeNull()
                .shouldBeInstanceOf<FabriktGenerateTask>()
                .configurations.get().shouldHaveSize(1)
                .first().run {
                    this.apiFile shouldContain apiFile
                    this.apiFragments.files should beEmpty()
                    this.basePackage shouldContainString basePackage
                    this.outputDirectory shouldContain outDir
                    this.sourcesPath shouldContain "src/main/kotlin"
                    this.resourcesPath shouldContain "src/main/resources"
                    this.typeOverrides.isPresent shouldBe false
                    this.validationLibrary shouldContain ValidationLibrary.JAKARTA_VALIDATION
                    this.quarkusReflectionConfig shouldContain false
                    with(client) {
                        enabled shouldContain false
                        target shouldContain ClientCodeGenTargetType.OK_HTTP
                        resilience4j shouldContain false
                        suspendModifier shouldContain false
                    }
                    with(controller) {
                        enabled shouldContain false
                        target shouldContain ControllerCodeGenTargetType.SPRING
                        authentication shouldContain false
                        suspendModifier shouldContain false
                    }
                    with(model) {
                        enabled shouldContain true
                        extensibleEnums shouldContain false
                        javaSerialization shouldContain false
                        quarkusReflection shouldContain false
                        micronautIntrospection shouldContain false
                        micronautReflection shouldContain false
                        includeCompanionObject shouldContain false
                        sealedInterfacesForOneOf shouldContain false
                    }
                }
        }

    }

})
