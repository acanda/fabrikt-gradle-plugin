package ch.acanda.gradle.fabrikt.build.generator

import ch.acanda.gradle.fabrikt.build.ExtensionGenerator
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import org.gradle.api.file.RegularFileProperty

@Suppress("LongMethod")
internal fun TypeSpec.Builder.fileProperty(name: String) = apply {
    addProperty(
        PropertySpec.builder(name, RegularFileProperty::class)
            .initializer("%N.fileProperty()", ExtensionGenerator.PROP_OBJECTS)
            .build()
    )
}
