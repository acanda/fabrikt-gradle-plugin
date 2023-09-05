package ch.acanda.gradle.fabrikt.generator

import com.cjbooms.fabrikt.cli.ClientCodeGenOptionType
import com.cjbooms.fabrikt.cli.CodeGenerationType
import java.nio.file.Path

internal data class FabriktArguments(
    val apiFile: Path,
    val apiFragments: Set<Path>,
    val basePackage: CharSequence,
    val outputDirectory: Path,
    val targets: Set<CodeGenerationType>,
    val httpClientOpts: Set<ClientCodeGenOptionType>,
) {
    fun getCliArgs(): Array<String> {
        @Suppress("ArgumentListWrapping")
        val args = mutableListOf(
            "--api-file", apiFile.toAbsolutePath().toString(),
            "--base-package", basePackage.toString(),
            "--output-directory", outputDirectory.toAbsolutePath().toString(),
        )
        apiFragments.forEach { fragment ->
            args.add("--api-fragment")
            args.add(fragment.toAbsolutePath().toString())
        }
        targets.forEach { target ->
            args.add("--targets")
            args.add(target.name)
        }
        httpClientOpts.forEach { option ->
            args.add("--http-client-opts")
            args.add(option.name)
        }
        return args.toTypedArray()
    }
}
