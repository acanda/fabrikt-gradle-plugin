package ch.acanda.gradle.fabrikt.build.generator

import ch.acanda.gradle.fabrikt.build.ExtensionGenerator
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import org.gradle.api.file.ConfigurableFileCollection

internal fun TypeSpec.Builder.filesProperty(name: String) = apply {
    addProperty(
        PropertySpec.builder(name, ConfigurableFileCollection::class)
            .initializer("%N.fileCollection()", ExtensionGenerator.PROP_OBJECTS)
            .build()
    )
    addFunction(
        FunSpec.builder(name)
            .addParameter(name, Any::class, KModifier.VARARG)
            .addStatement("this.%1N.setFrom(*%1N)", name)
            .build()
    )
    addFunction(
        FunSpec.builder(name)
            .addParameter(name, Iterable::class.parameterizedBy(Any::class))
            .addStatement("this.%1N.setFrom(%1N)", name)
            .build()
    )
}
