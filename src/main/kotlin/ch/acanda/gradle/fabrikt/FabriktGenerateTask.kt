package ch.acanda.gradle.fabrikt

import ch.acanda.gradle.fabrikt.generator.generate
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFile
import org.gradle.api.model.ObjectFactory
import org.gradle.api.problems.ProblemSpec
import org.gradle.api.problems.Problems
import org.gradle.api.problems.Severity
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.logging.progress.ProgressLogger
import org.gradle.internal.logging.progress.ProgressLoggerFactory
import javax.inject.Inject

typealias GenerateTaskConfigurationInitializer =
    GenerateTaskConfiguration.(source: GenerateTaskExtension, defaults: GenerateTaskDefaults) -> Unit

abstract class FabriktGenerateTask @Inject constructor(
    private val objects: ObjectFactory,
    problems: Problems
) : DefaultTask() {

    private val problemReporter = problems.forNamespace(FabriktPlugin.PLUGIN_ID)

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
                    problemReporter.rethrowing(e, generatorProblem(e, config.name))
                }
            }
        }
    }

    private fun generatorProblem(e: GeneratorException, name: String) = Action { problem: ProblemSpec ->
        problem
            .id("fabrikt-code-generation", "Fabrikt failed to generate code.")
            .contextualLabel("Fabrikt failed to generate code for configuration $name.")
            .details("Fabrikt failed to generate code for the OpenAPI specification ${e.apiFile}.")
            .severity(Severity.ERROR)
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
        progressLogger.completed(null, failed)
    }

}
