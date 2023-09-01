package ch.acanda.gradle.fabrikt.generator

import com.cjbooms.fabrikt.cli.CodeGenerationType
import java.nio.file.Path

internal data class FabriktArguments(
    val apiFile: Path,
    val basePackage: CharSequence,
    val outputDirectory: Path
) {
    fun getCliArgs(): Array<String> {
        @Suppress("ArgumentListWrapping")
        return arrayOf(
            "--api-file", apiFile.toAbsolutePath().toString(),
            "--base-package", basePackage.toString(),
            "--output-directory", outputDirectory.toAbsolutePath().toString(),
            "--targets", CodeGenerationType.HTTP_MODELS.name
        )
    }
}
