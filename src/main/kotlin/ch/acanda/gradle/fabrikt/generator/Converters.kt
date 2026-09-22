package ch.acanda.gradle.fabrikt.generator

import ch.acanda.gradle.fabrikt.CustomTypeMappingConfiguration
import ch.acanda.gradle.fabrikt.OperationIdTransformConfiguration

internal fun OperationIdTransformConfiguration.convertOperationIdTransform(): String? =
    if (regex.isPresent && replacement.isPresent) {
        "${regex.get()}:${replacement.get()}"
    } else {
        null
    }

internal fun CustomTypeMappingConfiguration.convertCustomTypeMapping(): String? =
    if (openApiType.isPresent && format.isPresent && kotlinType.isPresent) {
        val kotlinx = kotlinxSerializer.orNull?.let { ";kotlinx=$it" } ?: ""
        "${openApiType.get()}:${format.get()}=${kotlinType.get()}$kotlinx"
    } else {
        null
    }
