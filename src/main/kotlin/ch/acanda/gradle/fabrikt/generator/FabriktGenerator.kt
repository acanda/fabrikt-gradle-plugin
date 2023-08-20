package ch.acanda.gradle.fabrikt.generator

import com.cjbooms.fabrikt.cli.CodeGen
import java.nio.file.Path

internal fun generate(apiFile: Path, basePackage: String, outputDir: Path) {
    val args = FabriktArguments(apiFile, basePackage, outputDir)
    CodeGen.main(args.getCliArgs())
}
