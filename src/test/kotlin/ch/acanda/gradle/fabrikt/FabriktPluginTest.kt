package ch.acanda.gradle.fabrikt

import ch.acanda.gradle.fabrikt.matchers.shouldContain
import com.cjbooms.fabrikt.cli.ClientCodeGenTargetType
import com.cjbooms.fabrikt.cli.CodeGenerationType
import com.cjbooms.fabrikt.cli.ControllerCodeGenTargetType
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

class FabriktPluginTest : WordSpec({

    "The fabrikt plugin" should {
        "register the task fabriktGenerate" {
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
                    this.apiFile.get().asFile shouldBe apiFile
                    this.apiFragments.files should beEmpty()
                    this.basePackage.get() shouldBe basePackage
                    this.outputDirectory.get().asFile shouldBe outDir
                    this.targets.get() shouldContainExactly listOf(CodeGenerationType.HTTP_MODELS)
                    with(client) {
                        enabled shouldContain false
                        options.get() should beEmpty()
                        target shouldContain ClientCodeGenTargetType.OK_HTTP
                    }
                    with(controller) {
                        enabled shouldContain false
                        options.get() should beEmpty()
                        target shouldContain ControllerCodeGenTargetType.SPRING
                    }
                }
        }
    }

})
