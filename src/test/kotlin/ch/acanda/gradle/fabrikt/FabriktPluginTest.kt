package ch.acanda.gradle.fabrikt

import ch.acanda.gradle.fabrikt.FabriktPlugin.Companion.toTaskNameSuffix
import ch.acanda.gradle.fabrikt.matchers.shouldContain
import ch.acanda.gradle.fabrikt.matchers.shouldContainString
import io.kotest.core.spec.style.WordSpec
import io.kotest.engine.spec.tempdir
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.gradle.api.Project
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.testfixtures.ProjectBuilder

/**
 * Tests that the FabriktPlugin:
 * - creates its extensions
 * - registers the task `fabriktGenerate`
 *     - sets all its properties correctly
 *     - uses the proper defaults where the properties are not set
 * - registers the tasks `fabriktGenerate<Name>`
 *
 * It does not test the output of the task `fabriktGenerate`.
 */
class FabriktPluginTest : WordSpec({

    "The fabrikt plugin" should {
        "register the task fabriktGenerate with a full configuration" {
            val project = ProjectBuilder.builder().build()
            val apiFile = tempfile("apiSpec", ".yaml")
            val apiFragment = tempfile("apiFragment", ".yaml")
            val basePackage = "ch.acanda"
            val outputDirectory = tempdir("out")
            val srcDir = "src/fabrikt/kotlin"
            val resDir = "src/fabrikt/res"

            project.pluginManager.apply("ch.acanda.gradle.fabrikt")
            project.extensions.configure(FabriktExtension::class.java) { ext ->
                ext.generate("api") {
                    it.apiFile.set(apiFile)
                    it.apiFragments.setFrom(apiFragment)
                    it.externalReferenceResolution.set(it.aggressive)
                    it.basePackage.set(basePackage)
                    it.outputDirectory.set(outputDirectory)
                    it.sourcesPath.set(srcDir)
                    it.resourcesPath.set(resDir)
                    it.validationLibrary.set(it.Javax)
                    it.quarkusReflectionConfig.set(it.enabled)
                    with(it.typeOverrides) {
                        datetime.set(Instant)
                    }
                    with(it.client) {
                        generate.set(it.enabled)
                        target.set(OpenFeign)
                        resilience4j.set(it.enabled)
                        suspendModifier.set(it.enabled)
                    }
                    with(it.controller) {
                        generate.set(it.enabled)
                        target.set(Micronaut)
                        authentication.set(it.enabled)
                        suspendModifier.set(it.enabled)
                    }
                    with(it.model) {
                        generate.set(it.disabled)
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
                    this.externalReferenceResolution shouldContain ExternalReferencesResolutionOption.aggressive
                    this.basePackage shouldContainString basePackage
                    this.outputDirectory shouldContain outputDirectory
                    this.sourcesPath shouldContainString srcDir
                    this.resourcesPath shouldContainString resDir
                    this.validationLibrary shouldContain ValidationLibraryOption.Javax
                    this.quarkusReflectionConfig shouldContain true
                    with(typeOverrides) {
                        datetime shouldContain DateTimeOverrideOption.Instant
                    }
                    with(client) {
                        generate shouldContain true
                        resilience4j shouldContain true
                        suspendModifier shouldContain true
                        target shouldContain ClientTargetOption.OpenFeign
                    }
                    with(controller) {
                        generate shouldContain true
                        authentication shouldContain true
                        suspendModifier shouldContain true
                        target shouldContain ControllerTargetOption.Micronaut
                    }
                    with(model) {
                        generate shouldContain false
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
            val outputDirectory = project.layout.buildDirectory.dir("generated/sources/fabrikt").get().asFile

            project.pluginManager.apply("ch.acanda.gradle.fabrikt")
            project.extensions.configure(FabriktExtension::class.java) { ext ->
                ext.generate("api") {
                    it.apiFile.set(apiFile)
                    it.basePackage.set(basePackage)
                }
            }

            project.tasks.findByName("fabriktGenerate")
                .shouldNotBeNull()
                .shouldBeInstanceOf<FabriktGenerateTask>()
                .configurations.get().shouldHaveSize(1)
                .first().run {
                    this.apiFile shouldContain apiFile
                    this.apiFragments.files should beEmpty()
                    this.externalReferenceResolution shouldContain ExternalReferencesResolutionOption.targeted
                    this.basePackage shouldContainString basePackage
                    this.outputDirectory shouldContain outputDirectory
                    this.sourcesPath shouldContain "src/main/kotlin"
                    this.resourcesPath shouldContain "src/main/resources"
                    this.validationLibrary shouldContain ValidationLibraryOption.Jakarta
                    this.quarkusReflectionConfig shouldContain false
                    with(typeOverrides) {
                        datetime shouldContain DateTimeOverrideOption.OffsetDateTime
                    }
                    with(client) {
                        generate shouldContain false
                        target shouldContain ClientTargetOption.OkHttp
                        resilience4j shouldContain false
                        suspendModifier shouldContain false
                    }
                    with(controller) {
                        generate shouldContain false
                        target shouldContain ControllerTargetOption.Spring
                        authentication shouldContain false
                        suspendModifier shouldContain false
                    }
                    with(model) {
                        generate shouldContain true
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

        "register the tasks fabriktGenerate<Name>" {
            val project = ProjectBuilder.builder().build()
            val apiFile = tempfile("apiSpec", ".yaml")
            val basePackage = "ch.acanda"

            project.pluginManager.apply("ch.acanda.gradle.fabrikt")
            project.extensions.configure(FabriktExtension::class.java) { ext ->
                ext.generate("dog") {
                    it.apiFile.set(apiFile)
                    it.basePackage.set(basePackage)
                    it.skip.set(true)
                }
                ext.generate("cat") {
                    it.apiFile.set(apiFile)
                    it.basePackage.set(basePackage)
                    it.skip.set(true)
                }
            }

            project.evaluate()

            with(project.tasks) {
                names shouldContainAll listOf("fabriktGenerate", "fabriktGenerateDog", "fabriktGenerateCat")
                named("fabriktGenerateDog", FabriktGenerateTask::class.java).get().configurations.get().apply {
                    this shouldHaveSize 1
                    this.forEach { it.skip.get() shouldBe false }
                }
                named("fabriktGenerate", FabriktGenerateTask::class.java).get().configurations.get().apply {
                    this shouldHaveSize 2
                    this.forEach { it.skip.get() shouldBe true }
                }
                named("fabriktGenerateCat", FabriktGenerateTask::class.java).get().configurations.get().apply {
                    this shouldHaveSize 1
                    this.forEach { it.skip.get() shouldBe false }
                }
            }
        }

    }

    "toTaskNameSuffix()" should {
        "keep and upper case non-ascii characters" {
            "älg".toTaskNameSuffix() shouldBe "Älg"
        }
        "remove special characters" {
            "älg-rådjur#2".toTaskNameSuffix() shouldBe "ÄlgRådjur2"
        }
        "convert a string containing only non-alphanumeric characters to an empty string" {
            "+¦\"@*#%&¬/|()='?´^`~<> ,;.:-_[]{}".toTaskNameSuffix() shouldBe ""
        }
    }

}) {

    companion object {
        private fun Project.evaluate() {
            (this as ProjectInternal).evaluate()
        }
    }

}
