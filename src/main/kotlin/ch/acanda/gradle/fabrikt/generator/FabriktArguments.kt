package ch.acanda.gradle.fabrikt.generator

import com.cjbooms.fabrikt.cli.CodeGenerationType
import java.nio.file.Path

internal data class FabriktArguments(
    val apiFile: Path,
    val basePackage: String,
    val outputDirectory: Path
) {
    fun getCliArgs(): Array<String> {
        @Suppress("ArgumentListWrapping")
        return arrayOf(
            "--api-file", apiFile.toAbsolutePath().toString(),
            "--base-package", basePackage,
            "--output-directory", outputDirectory.toAbsolutePath().toString(),
            "--targets", CodeGenerationType.HTTP_MODELS.name
        )
    }
}
