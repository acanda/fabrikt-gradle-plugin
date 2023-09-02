package ch.acanda.gradle.fabrikt.generator

import com.cjbooms.fabrikt.cli.CodeGen
import com.cjbooms.fabrikt.cli.CodeGenerationType
import java.nio.file.Path

internal fun generate(apiFile: Path, basePackage: CharSequence, outputDir: Path, targets: Set<CodeGenerationType>) {
    val args = FabriktArguments(apiFile, basePackage, outputDir, targets)
    CodeGen.main(args.getCliArgs())
}
