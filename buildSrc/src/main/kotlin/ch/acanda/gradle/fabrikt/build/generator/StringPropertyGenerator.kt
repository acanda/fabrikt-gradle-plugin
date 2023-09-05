package ch.acanda.gradle.fabrikt.build.generator

import ch.acanda.gradle.fabrikt.build.ExtensionGenerator
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import org.gradle.api.provider.Property

internal fun TypeSpec.Builder.stringProperty(name: String) = apply {
    addProperty(
        PropertySpec.builder(name, Property::class.parameterizedBy(CharSequence::class))
            .initializer("%N.property(CharSequence::class.java)", ExtensionGenerator.PROP_OBJECTS)
            .build()
    )
    addFunction(
        FunSpec.builder(name)
            .addParameter(name, CharSequence::class)
            .addStatement("this.%1N.set(%1N)", name)
            .build()
    )
    addFunction(
        FunSpec.builder(name)
            .addParameter(name, provider<CharSequence>())
            .addStatement("this.%1N.set(%1N)", name)
            .build()
    )
}

