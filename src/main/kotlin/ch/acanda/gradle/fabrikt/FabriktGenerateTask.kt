package ch.acanda.gradle.fabrikt

import ch.acanda.gradle.fabrikt.generator.GeneratorException
import ch.acanda.gradle.fabrikt.generator.ValidationException
import ch.acanda.gradle.fabrikt.generator.generate
import ch.acanda.gradle.fabrikt.task.Progress
import ch.acanda.gradle.fabrikt.task.handleGeneratorException
import ch.acanda.gradle.fabrikt.task.handleValidationException
import org.gradle.api.DefaultTask
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskAction
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
                val specFile = config.apiFile.orElse(config.jsonSchema.file).get()
                val skip = config.skip.get()
                progress.log(specFile, skip)
                try {
                    generate(config)
                } catch (e: GeneratorException) {
                    progress.fail(specFile)
                    handleGeneratorException(e, specFile, config.name, services)
                } catch (e: ValidationException) {
                    e.ifContainsError { progress.fail(specFile) }
                    handleValidationException(e, config.name, services)
                }
            }
        }
    }

}
