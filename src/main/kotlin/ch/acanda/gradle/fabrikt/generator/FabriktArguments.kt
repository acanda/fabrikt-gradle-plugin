package ch.acanda.gradle.fabrikt.generator

import com.cjbooms.fabrikt.cli.CodeGenerationType
import java.nio.file.Path

internal data class FabriktArguments(
    val apiFile: Path,
    val basePackage: CharSequence,
    val outputDirectory: Path,
    val targets: Set<CodeGenerationType>
) {
    fun getCliArgs(): Array<String> {
        @Suppress("ArgumentListWrapping")
        val args = mutableListOf(
            "--api-file", apiFile.toAbsolutePath().toString(),
            "--base-package", basePackage.toString(),
            "--output-directory", outputDirectory.toAbsolutePath().toString(),
        )
        targets.forEach { target ->
            args.add("--targets")
            args.add(target.name)
        }
        return args.toTypedArray()
    }
}
