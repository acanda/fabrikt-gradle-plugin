package ch.acanda.gradle.fabrikt.generator

import ch.acanda.gradle.fabrikt.GenerateTaskConfiguration
import com.cjbooms.fabrikt.cli.ClientCodeGenOptionType
import com.cjbooms.fabrikt.cli.ClientCodeGenTargetType
import com.cjbooms.fabrikt.cli.CodeGenerationType
import com.cjbooms.fabrikt.cli.ControllerCodeGenOptionType
import com.cjbooms.fabrikt.cli.ControllerCodeGenTargetType
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.collections.shouldNotContainAnyOf
import io.kotest.matchers.collections.shouldNotContainInOrder
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.set
import io.kotest.property.arbitrary.stringPattern
import io.kotest.property.checkAll
import org.gradle.testfixtures.ProjectBuilder
import java.io.File
import java.nio.file.Paths

class FabriktArgumentsTest : StringSpec({

    "should handle any combination of arguments" {
        checkAll(generateTaskConfigGen) { config ->
            val cliArgs = FabriktArguments(config).getCliArgs()
            cliArgs shouldContainInOrder listOf(ARG_API_FILE, config.apiFile.asFile.get().absolutePath)
            cliArgs shouldContainInOrder listOf(ARG_BASE_PACKAGE, config.basePackage.get().toString())
            cliArgs shouldContainInOrder listOf(ARG_OUT_DIR, config.outputDirectory.asFile.get().absolutePath)
            config.apiFragments.forEach { fragment ->
                cliArgs shouldContainInOrder listOf("--api-fragment", fragment.absolutePath)
            }
            with(config.client) {
                if (enabled.get()) {
                    cliArgs shouldContainInOrder listOf(ARG_TARGETS, CodeGenerationType.CLIENT.name)
                    options.get().forEach { option ->
                        cliArgs shouldContainInOrder listOf(ARG_CLIENT_OPTS, option.name)
                    }
                    if (target.isPresent) {
                        cliArgs shouldContainInOrder listOf(ARG_CLIENT_TARGET, target.get().name)
                    } else {
                        cliArgs shouldNotContain ARG_CLIENT_TARGET
                    }

                } else {
                    cliArgs shouldNotContainInOrder listOf(ARG_TARGETS, CodeGenerationType.CLIENT.name)
                    cliArgs shouldNotContainAnyOf listOf(ARG_CLIENT_OPTS, ARG_CLIENT_TARGET)
                }
            }
            with(config.controller) {
                if (enabled.get()) {
                    cliArgs shouldContainInOrder listOf(ARG_TARGETS, CodeGenerationType.CONTROLLERS.name)
                    options.get().forEach { option ->
                        cliArgs shouldContainInOrder listOf(ARG_CONTROLLER_OPTS, option.name)
                    }
                    if (target.isPresent) {
                        cliArgs shouldContainInOrder listOf(ARG_CONTROLLER_TARGET, target.get().name)
                    } else {
                        cliArgs shouldNotContain ARG_CONTROLLER_TARGET
                    }
                } else {
                    cliArgs shouldNotContainInOrder listOf(ARG_TARGETS, CodeGenerationType.CONTROLLERS.name)
                    cliArgs shouldNotContainAnyOf listOf(ARG_CONTROLLER_OPTS, ARG_CONTROLLER_TARGET)
                }
            }
        }
    }

}) {

    companion object {

        private val generateTaskConfigGen: Arb<GenerateTaskConfiguration> = arbitrary {
            val project = ProjectBuilder.builder().build()
            GenerateTaskConfiguration(project).apply {
                apiFile.set(pathGen.bind())
                apiFragments.setFrom(Arb.set(pathGen, 0..3).bind())
                basePackage.set(Arb.stringPattern("[a-z]{1,5}(\\.[a-z]{1,5}){0,3}").bind())
                outputDirectory.set(pathGen.bind())
                targets.set(enumSet<CodeGenerationType>().bind())
                client.enabled.set(Arb.boolean().orNull(0.2).bind())
                client.options.set(enumSet<ClientCodeGenOptionType>().bind())
                client.target.set(Arb.enum<ClientCodeGenTargetType>().orNull(0.2).bind())
                controller.enabled.set(Arb.boolean().orNull(0.2).bind())
                controller.options.set(enumSet<ControllerCodeGenOptionType>().bind())
                controller.target.set(Arb.enum<ControllerCodeGenTargetType>().orNull(0.2).bind())
            }
        }

        private val pathGen: Arb<File> = arbitrary {
            Paths.get(Arb.stringPattern("[A-Za-z0-9]{1,5}(/[A-Za-z0-9]{1,5}){0,3}").bind()).toFile()
        }

        private inline fun <reified T : Enum<T>> enumSet(): Arb<Set<T>> = arbitrary { randomSource ->
            val values = T::class.java.enumConstants.toList().shuffled().toMutableList()
            var count = randomSource.random.nextInt(0, values.size + 1)
            while (count-- > 0) {
                values.removeLast()
            }
            values.toSet()
        }

    }

}
