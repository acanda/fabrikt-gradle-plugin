package ch.acanda.gradle.fabrikt.generator

import ch.acanda.gradle.fabrikt.GenerateTaskConfiguration
import ch.acanda.gradle.fabrikt.processor.processGeneratedSources
import com.cjbooms.fabrikt.cli.CodeGen
import com.cjbooms.fabrikt.util.ModelNameRegistry
import org.gradle.api.provider.Property
import java.io.File

internal fun generate(config: GenerateTaskConfiguration) {
    if (!config.skip.get()) {
        var arguments: Arguments
        try {
            config.sourcesDir().deleteRecursively()
            config.resourcesDir().deleteRecursively()
            ModelNameRegistry.clear()
            arguments = FabriktArguments(config).getCliArgs()
            if (!arguments.hasValidationErrors()) {
                CodeGen.main(arguments.args)
                processGeneratedSources(config)
            }
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            val spec = config.apiFile.get().asFile.toPath()
            throw GeneratorException(spec, e)
        }
        if (arguments.hasValidationProblems()) {
            throw ValidationException(arguments.problems)
        }
    }
}

private fun GenerateTaskConfiguration.sourcesDir(): File = generatedDir(sourcesPath)

private fun GenerateTaskConfiguration.resourcesDir(): File = generatedDir(resourcesPath)

private fun GenerateTaskConfiguration.generatedDir(path: Property<CharSequence>): File =
    outputDirectory.get()
        .dir(path.get().toString())
        .dir(basePackage.get().toString().replace('.', '/')).asFile
