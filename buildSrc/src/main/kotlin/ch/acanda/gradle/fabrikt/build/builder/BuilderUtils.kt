package ch.acanda.gradle.fabrikt.build.builder

import ch.acanda.gradle.fabrikt.build.GeneratePluginClassesTask
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.TypeName
import javax.annotation.processing.Generated

internal fun generated() = AnnotationSpec.builder(Generated::class)
    .addMember("\"${GeneratePluginClassesTask::class.qualifiedName}\"")
    .build()

internal fun TypeName.nullable(nullable: Boolean = true) =
    if (nullable) {
        copy(nullable = true)
    } else {
        this
    }

