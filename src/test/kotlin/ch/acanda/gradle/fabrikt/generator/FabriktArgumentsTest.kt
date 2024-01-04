package ch.acanda.gradle.fabrikt.generator

import ch.acanda.gradle.fabrikt.DateTimeOverrideType
import ch.acanda.gradle.fabrikt.FabriktOption
import ch.acanda.gradle.fabrikt.GenerateTaskConfiguration
import com.cjbooms.fabrikt.cli.ClientCodeGenOptionType
import com.cjbooms.fabrikt.cli.ClientCodeGenTargetType
import com.cjbooms.fabrikt.cli.CodeGenerationType
import com.cjbooms.fabrikt.cli.ControllerCodeGenOptionType
import com.cjbooms.fabrikt.cli.ControllerCodeGenTargetType
import com.cjbooms.fabrikt.cli.ModelCodeGenOptionType
import com.cjbooms.fabrikt.cli.ValidationLibrary
import io.kotest.assertions.print.print
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.collections.shouldNotContainAnyOf
import io.kotest.matchers.collections.shouldNotContainInOrder
import io.kotest.matchers.neverNullMatcher
import io.kotest.matchers.should
import io.kotest.matchers.shouldNot
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.set
import io.kotest.property.arbitrary.stringPattern
import io.kotest.property.checkAll
import org.gradle.api.provider.Provider
import org.gradle.testfixtures.ProjectBuilder
import java.io.File
import java.nio.file.Paths

class FabriktArgumentsTest : StringSpec({

    "should handle any combination of arguments" {
        checkAll(generateTaskConfigGen) { config ->
            val cliArgs = FabriktArguments(config).getCliArgs()
            cliArgs shouldNotContain "null"
            cliArgs shouldContainInOrder listOf(ARG_API_FILE, config.apiFile.asFile.get().absolutePath)
            cliArgs shouldContainInOrder listOf(ARG_BASE_PACKAGE, config.basePackage.get().toString())
            cliArgs shouldContainInOrder listOf(ARG_OUT_DIR, config.outputDirectory.asFile.get().absolutePath)
            cliArgs shouldContainInOrder listOf(ARG_SRC_PATH, config.sourcesPath.get().toString())
            cliArgs shouldContainInOrder listOf(ARG_RESOURCES_PATH, config.resourcesPath.get().toString())
            cliArgs.shouldContainOptionally(config.validationLibrary, ARG_VALIDATION_LIB)
            cliArgs.shouldContainOptionally(
                config.quarkusReflectionConfig,
                ARG_TARGETS,
                CodeGenerationType.QUARKUS_REFLECTION_CONFIG
            )
            config.apiFragments.forEach { fragment ->
                cliArgs shouldContainInOrder listOf("--api-fragment", fragment.absolutePath)
            }
            with(config.typeOverrides) {
                cliArgs.shouldContainOptionallyEnum(datetime, ARG_TYPE_OVERRIDES)
            }
            with(config.client) {
                if (enabled.get()) {
                    cliArgs shouldContainInOrder listOf(ARG_TARGETS, CodeGenerationType.CLIENT.name)
                    cliArgs.shouldContainOptionally(resilience4j, ARG_CLIENT_OPTS, ClientCodeGenOptionType.RESILIENCE4J)
                    cliArgs.shouldContainOptionally(
                        suspendModifier,
                        ARG_CLIENT_OPTS,
                        ClientCodeGenOptionType.SUSPEND_MODIFIER
                    )
                    cliArgs.shouldContainOptionally(target, ARG_CLIENT_TARGET)

                } else {
                    cliArgs shouldNotContainInOrder listOf(ARG_TARGETS, CodeGenerationType.CLIENT.name)
                    cliArgs shouldNotContainAnyOf listOf(ARG_CLIENT_OPTS, ARG_CLIENT_TARGET)
                }
            }
            with(config.controller) {
                if (enabled.get()) {
                    cliArgs shouldContainInOrder listOf(ARG_TARGETS, CodeGenerationType.CONTROLLERS.name)
                    cliArgs.shouldContainOptionally(
                        authentication,
                        ARG_CONTROLLER_OPTS,
                        ControllerCodeGenOptionType.AUTHENTICATION
                    )
                    cliArgs.shouldContainOptionally(
                        suspendModifier,
                        ARG_CONTROLLER_OPTS,
                        ControllerCodeGenOptionType.SUSPEND_MODIFIER
                    )
                    cliArgs.shouldContainOptionally(target, ARG_CONTROLLER_TARGET)
                } else {
                    cliArgs shouldNotContainInOrder listOf(ARG_TARGETS, CodeGenerationType.CONTROLLERS.name)
                    cliArgs shouldNotContainAnyOf listOf(ARG_CONTROLLER_OPTS, ARG_CONTROLLER_TARGET)
                }
            }
            with(config.model) {
                if (enabled.get()) {
                    cliArgs shouldContainInOrder listOf(ARG_TARGETS, CodeGenerationType.HTTP_MODELS.name)
                    cliArgs.shouldContainOptionally(
                        extensibleEnums,
                        ARG_MODEL_OPTS,
                        ModelCodeGenOptionType.X_EXTENSIBLE_ENUMS
                    )
                    cliArgs.shouldContainOptionally(
                        javaSerialization,
                        ARG_MODEL_OPTS,
                        ModelCodeGenOptionType.JAVA_SERIALIZATION
                    )
                    cliArgs.shouldContainOptionally(
                        quarkusReflection,
                        ARG_MODEL_OPTS,
                        ModelCodeGenOptionType.QUARKUS_REFLECTION
                    )
                    cliArgs.shouldContainOptionally(
                        micronautIntrospection,
                        ARG_MODEL_OPTS,
                        ModelCodeGenOptionType.MICRONAUT_INTROSPECTION
                    )
                    cliArgs.shouldContainOptionally(
                        micronautReflection,
                        ARG_MODEL_OPTS,
                        ModelCodeGenOptionType.MICRONAUT_REFLECTION
                    )
                    cliArgs.shouldContainOptionally(
                        includeCompanionObject,
                        ARG_MODEL_OPTS,
                        ModelCodeGenOptionType.INCLUDE_COMPANION_OBJECT
                    )
                    cliArgs.shouldContainOptionally(
                        sealedInterfacesForOneOf,
                        ARG_MODEL_OPTS,
                        ModelCodeGenOptionType.SEALED_INTERFACES_FOR_ONE_OF
                    )
                } else {
                    cliArgs shouldNotContainInOrder listOf(ARG_TARGETS, CodeGenerationType.HTTP_MODELS.name)
                    cliArgs shouldNotContain ARG_MODEL_OPTS
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
                sourcesPath.set(Arb.stringPattern("[a-z]{1,5}(/[a-z]{1,5}){0,3}").orNull(0.2).bind())
                resourcesPath.set(Arb.stringPattern("[a-z]{1,5}(/[a-z]{1,5}){0,3}").orNull(0.2).bind())
                typeOverrides.datetime.set(Arb.enum<DateTimeOverrideType>().orNull(0.2).bind())
                validationLibrary.set(Arb.enum<ValidationLibrary>().orNull(0.2).bind())
                client.enabled.set(Arb.boolean().orNull(0.2).bind())
                client.resilience4j.set(Arb.boolean().orNull(0.2).bind())
                client.suspendModifier.set(Arb.boolean().orNull(0.2).bind())
                client.target.set(Arb.enum<ClientCodeGenTargetType>().orNull(0.2).bind())
                controller.enabled.set(Arb.boolean().orNull(0.2).bind())
                controller.authentication.set(Arb.boolean().orNull(0.2).bind())
                controller.suspendModifier.set(Arb.boolean().orNull(0.2).bind())
                controller.target.set(Arb.enum<ControllerCodeGenTargetType>().orNull(0.2).bind())
                model.enabled.set(Arb.boolean().orNull(0.2).bind())
                model.extensibleEnums.set(Arb.boolean().orNull(0.2).bind())
                model.javaSerialization.set(Arb.boolean().orNull(0.2).bind())
                model.quarkusReflection.set(Arb.boolean().orNull(0.2).bind())
                model.micronautIntrospection.set(Arb.boolean().orNull(0.2).bind())
                model.micronautReflection.set(Arb.boolean().orNull(0.2).bind())
                model.includeCompanionObject.set(Arb.boolean().orNull(0.2).bind())
                model.sealedInterfacesForOneOf.set(Arb.boolean().orNull(0.2).bind())
            }
        }

        private val pathGen: Arb<File> = arbitrary {
            Paths.get(Arb.stringPattern("[A-Za-z0-9]{1,5}(/[A-Za-z0-9]{1,5}){0,3}").bind()).toFile()
        }

        private fun <E : Enum<E>> Array<String>.shouldContainOptionally(valueProvider: Provider<E>, arg: String) {
            if (valueProvider.isPresent) {
                this shouldContainInOrder listOf(arg, valueProvider.get().name)
            } else {
                this shouldNotContain arg
            }
        }

        private fun Array<String>.shouldContainOptionally(
            valueProvider: Provider<Boolean>,
            argName: String,
            argValue: Enum<*>
        ) {
            val containArgument = containsArgument(argName, argValue.name)
            if (valueProvider.getOrElse(false)) {
                this should containArgument
            } else {
                this shouldNot containArgument
            }
        }

        private fun Array<String>.shouldContainOptionallyEnum(
            valueProvider: Provider<out FabriktOption>,
            argName: String
        ) {
            val argValue = valueProvider.orNull?.fabriktOption?.name
            if (argValue != null) {
                val containArgument = containsArgument(argName, argValue)
                this should containArgument
            }
        }

        private fun containsArgument(name: String, value: String): Matcher<Array<String>> =
            neverNullMatcher { actual ->
                val actualIterator = actual.iterator()

                var contains = false
                while (actualIterator.hasNext() && !contains) {
                    if (actualIterator.next() == name && actualIterator.hasNext() && actualIterator.next() == value) {
                        contains = true
                    }
                }

                MatcherResult(
                    contains,
                    { "${actual.print().value} did not contain the argument $name $value" },
                    { "${actual.print().value} should not contain the argument $name $value" }
                )
            }

    }

}
