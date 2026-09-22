package ch.acanda.gradle.fabrikt

import ch.acanda.gradle.fabrikt.generator.GeneratorException
import ch.acanda.gradle.fabrikt.generator.ValidationException
import ch.acanda.gradle.fabrikt.generator.ValidationProblem
import ch.acanda.gradle.fabrikt.generator.ValidationSeverity
import ch.acanda.gradle.fabrikt.generator.generate
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFile
import org.gradle.api.model.ObjectFactory
import org.gradle.api.problems.ProblemGroup
import org.gradle.api.problems.ProblemId
import org.gradle.api.problems.ProblemSpec
import org.gradle.api.problems.Problems
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.logging.progress.ProgressLogger
import org.gradle.internal.logging.progress.ProgressLoggerFactory
import javax.inject.Inject

@CacheableTask
abstract class FabriktGenerateTask @Inject constructor(
    private val objects: ObjectFactory
) : DefaultTask() {

    @get:Nested
    abstract val configurations: ListProperty<GenerateTaskConfiguration>

    fun addConfigurations(
        configsProvider: Provider<out List<GenerateTaskExtension>>,
        defaultsProvider: Provider<out GenerateTaskDefaults>,
        initializer: GenerateTaskConfigurationInitializer
    ) {
        configurations.addAll(
            configsProvider.map { configs ->
                val defaults = defaultsProvider.get()
                configs.map { config ->
                    objects.newInstance(GenerateTaskConfiguration::class.java, name)
                        .also { initializer.invoke(it, config, defaults) }
                }
            }
        )
    }

    @TaskAction
    fun generate() {
        val progressLoggerFactory = services.get(ProgressLoggerFactory::class.java)
        val configs = configurations.get()
        Progress(progressLoggerFactory, configs.size).use { progress ->
            configs.forEach { config ->
                val apiFile = config.apiFile.get()
                val skip = config.skip.get()
                progress.log(apiFile, skip)
                try {
                    generate(config)
                } catch (e: GeneratorException) {
                    progress.fail(apiFile)
                    handleGeneratorException(e, apiFile, config.name)
                } catch (e: ValidationException) {
                    e.ifContainsError { progress.fail(apiFile) }
                    handleValidationException(e, config.name)
                }
            }
        }
    }

    @Suppress("UnstableApiUsage")
    private fun handleGeneratorException(
        e: GeneratorException,
        apiFile: RegularFile,
        configName: String
    ) {
        val problemReporter = services.get(Problems::class.java).reporter
        val id = ProblemId.create("generator", "Fabrikt failed to generate code.", PROBLEM_GROUP)
        problemReporter.throwing(e, id, generatorProblem(apiFile, configName))
    }

    @Suppress("UnstableApiUsage")
    private fun handleValidationException(e: ValidationException, configName: String) {
        val problemReporter = services.get(Problems::class.java).reporter
        val id = ProblemId.create("validation", "Invalid value in Fabrikt settings.", PROBLEM_GROUP)
        e.problems.forEach { problem ->
            when (problem.severity) {
                ValidationSeverity.WARNING -> problemReporter.report(id, validationProblem(problem, configName))
                ValidationSeverity.ERROR -> problemReporter.throwing(e, id, validationProblem(problem, configName))
            }
        }
    }

    @Suppress("UnstableApiUsage")
    private fun generatorProblem(apiFile: RegularFile, name: String) =
        Action { problem: ProblemSpec ->
            problem
                .contextualLabel("Fabrikt failed to generate code for configuration $name.")
                .details("Fabrikt failed to generate code for the OpenAPI specification $apiFile.")
        }

    @Suppress("UnstableApiUsage")
    private fun validationProblem(e: ValidationProblem, name: String) =
        Action { problem: ProblemSpec ->
            problem
                .contextualLabel("Invalid Fabrikt setting in configuration $name.")
                .details(e.message.orEmpty())
        }

    companion object {
        @Suppress("UnstableApiUsage")
        private val PROBLEM_GROUP = ProblemGroup.create("fabrikt", "Fabrikt Code Generation")
    }

}

private class Progress(factory: ProgressLoggerFactory, val total: Int) : AutoCloseable {

    private val progressLogger: ProgressLogger = factory.newOperation(FabriktGenerateTask::class.java)
    private var count = 0
    private var failed = false

    init {
        progressLogger.start("Generating Kotlin code with Fabrikt", "[0/$total]")
    }

    fun log(apiFile: RegularFile, skip: Boolean) {
        count++
        val skipMsg = if (skip) " skip" else ""
        progressLogger.progress("[$count/$total]$skipMsg generating code for $apiFile...")
    }

    fun fail(apiFile: RegularFile) {
        failed = true
        progressLogger.progress("[$count/$total] generating code for $apiFile...", true)
    }

    override fun close() {
        progressLogger.completed("", failed)
    }

}
