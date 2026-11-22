package ch.acanda.gradle.fabrikt.generator

import ch.acanda.gradle.fabrikt.FabriktPluginException

class GeneratorException(val specFile: String, cause: Throwable?) :
    FabriktPluginException("Failed to generate code for specification $specFile.", cause)

class ValidationException(val problems: List<ValidationProblem>) :
    FabriktPluginException(problems.joinToString("\n") { "[${it.severity}] ${it.message}" }) {

    fun ifContainsError(action: (List<ValidationProblem>) -> Unit) {
        if (problems.any { it.severity == ValidationSeverity.ERROR }) {
            action(problems)
        }
    }

}
