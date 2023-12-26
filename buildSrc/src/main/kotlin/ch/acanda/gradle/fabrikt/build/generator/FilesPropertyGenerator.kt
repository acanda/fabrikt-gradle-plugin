package ch.acanda.gradle.fabrikt.build.generator

import ch.acanda.gradle.fabrikt.build.ExtensionGenerator
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import org.gradle.api.file.ConfigurableFileCollection

internal fun TypeSpec.Builder.filesProperty(name: String) = apply {
    addProperty(
        PropertySpec.builder(name, ConfigurableFileCollection::class)
            .initializer("%N.fileCollection()", ExtensionGenerator.PROP_OBJECTS)
            .build()
    )
}
