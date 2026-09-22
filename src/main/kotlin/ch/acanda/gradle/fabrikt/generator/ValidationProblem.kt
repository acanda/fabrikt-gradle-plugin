package ch.acanda.gradle.fabrikt.generator

class ValidationProblem private constructor(val message: String, val severity: ValidationSeverity) {

    fun ifError(action: (ValidationProblem) -> Unit) {
        if (severity == ValidationSeverity.ERROR) {
            action(this)
        }
    }

    companion object {
        fun warning(message: String): ValidationProblem = ValidationProblem(message, ValidationSeverity.WARNING)
        fun error(message: String): ValidationProblem = ValidationProblem(message, ValidationSeverity.ERROR)
    }

}

enum class ValidationSeverity {
    WARNING,
    ERROR
}
