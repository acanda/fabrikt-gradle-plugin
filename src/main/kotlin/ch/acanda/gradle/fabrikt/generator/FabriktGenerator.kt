package ch.acanda.gradle.fabrikt.generator

import ch.acanda.gradle.fabrikt.GenerateTaskConfiguration
import ch.acanda.gradle.fabrikt.processor.processGeneratedSources
import com.cjbooms.fabrikt.cli.CodeGen
import com.cjbooms.fabrikt.util.ModelNameRegistry

internal fun generate(config: GenerateTaskConfiguration) {
    ModelNameRegistry.clear()
    val args = FabriktArguments(config)
    CodeGen.main(args.getCliArgs())
    processGeneratedSources(config)
}
