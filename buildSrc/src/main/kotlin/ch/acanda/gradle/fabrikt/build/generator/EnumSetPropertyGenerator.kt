package ch.acanda.gradle.fabrikt.build.generator

import ch.acanda.gradle.fabrikt.build.ExtensionGenerator
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import org.gradle.api.provider.SetProperty
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

internal fun TypeSpec.Builder.enumSetProperty(name: String, enumType: KClass<out Enum<*>>) = apply {
    addProperty(
        PropertySpec.builder(name, SetProperty::class.parameterizedBy(enumType))
            .initializer(
                "%1N.setProperty(%2T::class.java).convention(null·as·Set<%2T>?)",
                ExtensionGenerator.PROP_OBJECTS,
                enumType
            )
            .build()
    )
    addFunction(
        FunSpec.builder(name)
            .addParameter(name, enumType, KModifier.VARARG)
            .addStatement("this.%1N.set(%1N.toSet())", name)
            .build()
    )
    addFunction(
        FunSpec.builder(name)
            .addParameter(name, Iterable::class.parameterizedBy(enumType))
            .addStatement("this.%1N.set(%1N.toSet())", name)
            .build()
    )
    addFunction(
        FunSpec.builder(name)
            .addParameter(name, provider(Iterable::class.parameterizedBy(enumType)))
            .addStatement("this.%1N.set(%1N)", name)
            .build()
    )
    enumType.java.enumConstants.iterator().forEach { enumValue ->
        val spec = PropertySpec.builder(enumValue.name, enumType).initializer("%T.%N", enumType, enumValue.name)
        enumValue::class.memberProperties
            .find { it.name == "description" }
            ?.let {
                spec.addKdoc(it.getter.call(enumValue) as String)
            }
        addProperty(spec.build())
    }
}
