package ch.acanda.gradle.fabrikt.generator

import ch.acanda.gradle.fabrikt.FabriktOption
import ch.acanda.gradle.fabrikt.generateTaskConfigGen
import com.cjbooms.fabrikt.cli.ClientCodeGenOptionType
import com.cjbooms.fabrikt.cli.CodeGenerationType
import com.cjbooms.fabrikt.cli.ControllerCodeGenOptionType
import com.cjbooms.fabrikt.cli.ModelCodeGenOptionType
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
import io.kotest.property.checkAll
import org.gradle.api.provider.Provider

class FabriktArgumentsTest : StringSpec({

    "should handle any combination of arguments" {
        checkAll(generateTaskConfigGen) { config ->
            val cliArgs = FabriktArguments(config).getCliArgs()
            cliArgs shouldNotContain "null"
            cliArgs shouldContainInOrder listOf(ARG_API_FILE, config.apiFile.asFile.get().absolutePath)
            cliArgs.shouldContainOptionally(config.externalReferenceResolution, ARG_EXT_REF_RESOLUTION)
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
                cliArgs.shouldContainOptionally(datetime, ARG_TYPE_OVERRIDES)
                cliArgs.shouldContainOptionally(binary, ARG_TYPE_OVERRIDES)
            }
            with(config.client) {
                if (generate.get()) {
                    cliArgs shouldContainInOrder listOf(ARG_TARGETS, CodeGenerationType.CLIENT.name)
                    cliArgs.shouldContainOptionally(resilience4j, ARG_CLIENT_OPTS, ClientCodeGenOptionType.RESILIENCE4J)
                    cliArgs.shouldContainOptionally(
                        suspendModifier,
                        ARG_CLIENT_OPTS,
                        ClientCodeGenOptionType.SUSPEND_MODIFIER
                    )
                    cliArgs.shouldContainOptionally(
                        springResponseEntityWrapper,
                        ARG_CLIENT_OPTS,
                        ClientCodeGenOptionType.SPRING_RESPONSE_ENTITY_WRAPPER
                    )
                    cliArgs.shouldContainOptionally(target, ARG_CLIENT_TARGET)

                } else {
                    cliArgs shouldNotContainInOrder listOf(ARG_TARGETS, CodeGenerationType.CLIENT.name)
                    cliArgs shouldNotContainAnyOf listOf(ARG_CLIENT_OPTS, ARG_CLIENT_TARGET)
                }
            }
            with(config.controller) {
                if (generate.get()) {
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
                    cliArgs.shouldContainOptionally(
                        completionStage,
                        ARG_CONTROLLER_OPTS,
                        ControllerCodeGenOptionType.COMPLETION_STAGE
                    )
                    cliArgs.shouldContainOptionally(target, ARG_CONTROLLER_TARGET)
                } else {
                    cliArgs shouldNotContainInOrder listOf(ARG_TARGETS, CodeGenerationType.CONTROLLERS.name)
                    cliArgs shouldNotContainAnyOf listOf(ARG_CONTROLLER_OPTS, ARG_CONTROLLER_TARGET)
                }
            }
            with(config.model) {
                if (generate.get()) {
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
                    cliArgs.shouldContainOptionally(
                        nonNullMapValues,
                        ARG_MODEL_OPTS,
                        ModelCodeGenOptionType.NON_NULL_MAP_VALUES
                    )
                    cliArgs.shouldContainOptionally(
                        suffix,
                        ARG_MODEL_SUFFIX
                    )
                    cliArgs.shouldContainOptionally(
                        serializationLibrary,
                        ARG_MODEL_SERIALIZATION_LIB,
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

        @JvmName(name = "shouldContainBooleanOptionOptionally")
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

        @JvmName(name = "shouldContainOptionOptionally")
        private fun Array<String>.shouldContainOptionally(
            valueProvider: Provider<out FabriktOption>,
            argName: String
        ) {
            val argValue = valueProvider.orNull?.fabriktOption?.name
            if (argValue != null) {
                val containArgument = containsArgument(argName, argValue)
                this should containArgument
            }
        }

        @JvmName(name = "shouldContainStringOptionally")
        private fun Array<String>.shouldContainOptionally(
            valueProvider: Provider<out CharSequence>,
            argName: String
        ) {
            val argValue = valueProvider.orNull
            if (argValue != null) {
                val containArgument = containsArgument(argName, argValue.toString())
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
