package ch.acanda.gradle.fabrikt

import ch.acanda.gradle.fabrikt.generator.GeneratorException
import ch.acanda.gradle.fabrikt.generator.ValidationException
import ch.acanda.gradle.fabrikt.generator.generate
import ch.acanda.gradle.fabrikt.task.Progress
import ch.acanda.gradle.fabrikt.task.handleGeneratorException
import ch.acanda.gradle.fabrikt.task.handleValidationException
import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.problems.Problems
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.logging.progress.ProgressLoggerFactory
import org.gradle.work.DisableCachingByDefault
import java.util.logging.Logger
import javax.inject.Inject

@DisableCachingByDefault
abstract class FabriktPrintDependenciesTask @Inject constructor(
    private val objects: ObjectFactory,
    private val layout: ProjectLayout,
    private val problems: Problems
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
                    objects.newInstance(GenerateTaskConfiguration::class.java, config.name)
                        .also { initializer.invoke(it, config, defaults) }
                        .also { it.outputDirectory.set(layout.buildDirectory.dir("tmp/fabrikt/${it.name}")) }
                }
            }
        )
    }

    @TaskAction
    fun generate() {
        val progressLoggerFactory = services.get(ProgressLoggerFactory::class.java)
        val configs = configurations.get()
        Progress(progressLoggerFactory, configs.size).use { progress ->
            configs.forEach { config -> generate(config, progress) }
        }
    }

    private fun generate(config: GenerateTaskConfiguration, progress: Progress) {
        val specFile = config.apiFile.orElse(config.jsonSchema.file).get()
        val skip = config.skip.get()
        progress.log(specFile, skip)
        if (!skip) {
            val loggerFilter = Logger.getGlobal().filter
            try {
                // Disable logging from Fabrikt CLI to avoid cluttering the Gradle output
                Logger.getGlobal().filter = { false }
                generate(config, "--dependencies-generation-mode", "GRADLE_NOTATION")
                printDependencies(config, config.outputDirectory.get())
            } catch (e: GeneratorException) {
                progress.fail(specFile)
                handleGeneratorException(e, specFile, config.name, problems)
            } catch (e: ValidationException) {
                e.ifContainsError { progress.fail(specFile) }
                handleValidationException(e, config.name, problems)
            } finally {
                Logger.getGlobal().filter = loggerFilter
            }
        }
    }

    private fun printDependencies(config: GenerateTaskConfiguration, outputDirectory: Directory) {
        val dependenciesFile = outputDirectory.file("dependencies").asFile
        if (dependenciesFile.exists()) {
            val dependencies = dependenciesFile.readText().prependIndent("  ")
            logger.quiet("\nDependencies for configuration ${config.name}:\n$dependencies")
        } else {
            logger.error("\nDependencies file not found for configuration ${config.name}.")
        }
    }

}
