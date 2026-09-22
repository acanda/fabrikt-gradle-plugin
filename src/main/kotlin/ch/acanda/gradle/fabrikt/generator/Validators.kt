package ch.acanda.gradle.fabrikt.generator

import ch.acanda.gradle.fabrikt.OperationIdTransformConfiguration

internal fun OperationIdTransformConfiguration.validate(problems: MutableList<ValidationProblem>):
    OperationIdTransformConfiguration {
    if (regex.orNull?.contains(":") ?: false) {
        problems.add(
            ValidationProblem.error(
                "operationIdTransform.regex cannot contain ':' because Fabrikt uses" +
                    " the first ':' as the separator between the regex and the replacement."
            )
        )
    }
    if (regex.isPresent && !replacement.isPresent) {
        problems.add(
            ValidationProblem.warning(
                "operationIdTransform.regex is set but operationIdTransform.replacement is not set." +
                    " Fabrikt will not transform the operationId from the OpenAPI specification."
            )
        )
    }
    if (!regex.isPresent && replacement.isPresent) {
        problems.add(
            ValidationProblem.warning(
                "operationIdTransform.regex is not set but operationIdTransform.replacement is set." +
                    " Fabrikt will not transform the operationId from the OpenAPI specification."
            )
        )
    }
    return this
}
