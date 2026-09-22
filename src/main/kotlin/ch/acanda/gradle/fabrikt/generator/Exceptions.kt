package ch.acanda.gradle.fabrikt.generator

import ch.acanda.gradle.fabrikt.FabriktPluginException
import java.nio.file.Path
import kotlin.io.path.absolutePathString

class GeneratorException(val apiFile: Path, cause: Throwable?) :
    FabriktPluginException("Failed to generate code for OpenApi specification ${apiFile.absolutePathString()}.", cause)

class ValidationException(val problems: List<ValidationProblem>) :
    FabriktPluginException(problems.joinToString("\n") { "[${it.severity}] ${it.message}" }) {

    fun ifContainsError(action: (List<ValidationProblem>) -> Unit) {
        if (problems.any { it.severity == ValidationSeverity.ERROR }) {
            action(problems)
        }
    }

}
