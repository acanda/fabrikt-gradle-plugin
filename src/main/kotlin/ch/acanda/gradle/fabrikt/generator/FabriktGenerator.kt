package ch.acanda.gradle.fabrikt.generator

import ch.acanda.gradle.fabrikt.GenerateTaskConfiguration
import ch.acanda.gradle.fabrikt.GeneratorException
import ch.acanda.gradle.fabrikt.processor.processGeneratedSources
import com.cjbooms.fabrikt.cli.CodeGen
import com.cjbooms.fabrikt.util.ModelNameRegistry

internal fun generate(config: GenerateTaskConfiguration) {
    try {
        ModelNameRegistry.clear()
        val args = FabriktArguments(config)
        CodeGen.main(args.getCliArgs())
        processGeneratedSources(config)
    } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
        val spec = config.apiFile.get().asFile.toPath()
        throw GeneratorException(spec, e)
    }
}
