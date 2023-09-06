package ch.acanda.gradle.fabrikt.generator

import com.cjbooms.fabrikt.cli.ClientCodeGenOptionType
import com.cjbooms.fabrikt.cli.ClientCodeGenTargetType
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
    httpClientTarget: ClientCodeGenTargetType?,
) {
    val args =
        FabriktArguments(apiFile, apiFragments, basePackage, outputDir, targets, httpClientOpts, httpClientTarget)
    CodeGen.main(args.getCliArgs())
}
