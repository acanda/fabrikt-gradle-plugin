package ch.acanda.gradle.fabrikt.generator

import com.cjbooms.fabrikt.cli.CodeGen
import java.nio.file.Path

internal fun generate(apiFile: Path, basePackage: String, outputDir: Path) {
    @Suppress("ArgumentListWrapping")
    val args = arrayOf(
        "--api-file", apiFile.toAbsolutePath().toString(),
        "--base-package", basePackage,
        "--output-directory", outputDir.toAbsolutePath().toString(),
        "--targets", "HTTP_MODELS"
    )
    CodeGen.main(args)
}
