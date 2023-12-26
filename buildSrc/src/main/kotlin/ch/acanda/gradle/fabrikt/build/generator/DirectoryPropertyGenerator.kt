package ch.acanda.gradle.fabrikt.build.generator

import ch.acanda.gradle.fabrikt.build.ExtensionGenerator
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import org.gradle.api.file.DirectoryProperty

@Suppress("LongMethod")
internal fun TypeSpec.Builder.directoryProperty(name: String) = apply {
    addProperty(
        PropertySpec.builder(name, DirectoryProperty::class)
            .initializer("%N.directoryProperty()", ExtensionGenerator.PROP_OBJECTS)
            .build()
    )
}
