package ch.acanda.gradle.fabrikt.generator

import ch.acanda.gradle.fabrikt.OperationIdTransformConfiguration

internal fun OperationIdTransformConfiguration.convertOperationIdTransform(): String? =
    if (regex.isPresent && replacement.isPresent) {
        "${regex.get()}:${replacement.get()}"
    } else {
        null
    }
