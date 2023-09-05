package ch.acanda.gradle.fabrikt.generator

import com.cjbooms.fabrikt.cli.ClientCodeGenOptionType
import com.cjbooms.fabrikt.cli.CodeGen
import com.cjbooms.fabrikt.cli.CodeGenerationType
import java.nio.file.Path

@Suppress("LongParameterList")
internal fun generate(
    apiFile: Path,
    apiFragments: Set<Path>,
    basePackage: CharSequence,
    outputDir: Path,
    targets: Set<CodeGenerationType>,
    httpClientOpts: Set<ClientCodeGenOptionType>,
) {
    val args = FabriktArguments(apiFile, apiFragments, basePackage, outputDir, targets, httpClientOpts)
    CodeGen.main(args.getCliArgs())
}
