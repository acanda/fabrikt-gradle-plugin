package ch.acanda.gradle.fabrikt.generator

import ch.acanda.gradle.fabrikt.GenerateTaskConfiguration
import com.cjbooms.fabrikt.cli.CodeGen

internal fun generate(config: GenerateTaskConfiguration) {
    val args = FabriktArguments(config)
    CodeGen.main(args.getCliArgs())
}
