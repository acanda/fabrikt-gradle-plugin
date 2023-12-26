package ch.acanda.gradle.fabrikt.build.generator

import ch.acanda.gradle.fabrikt.build.ExtensionGenerator
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import org.gradle.api.provider.Property

internal fun TypeSpec.Builder.booleanProperty(name: String) = apply {
    addProperty(
        PropertySpec.builder(name, Property::class.parameterizedBy(Boolean::class))
            .initializer("%N.property(Boolean::class.java)", ExtensionGenerator.PROP_OBJECTS)
            .build()
    )
}
