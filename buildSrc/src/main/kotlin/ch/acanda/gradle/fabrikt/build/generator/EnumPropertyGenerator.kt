package ch.acanda.gradle.fabrikt.build.generator

import ch.acanda.gradle.fabrikt.build.ExtensionGenerator
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import kotlin.reflect.KClass
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberProperties

internal fun TypeSpec.Builder.enumProperty(name: String, enumType: KClass<out Enum<*>>) = apply {
    addProperty(
        PropertySpec.builder(name, Property::class.parameterizedBy(enumType))
            .initializer("%1N.property(%2T::class.java)", ExtensionGenerator.PROP_OBJECTS, enumType)
            .build()
    )
    addFunction(
        FunSpec.builder(name)
            .addParameter(name, enumType)
            .addStatement("this.%1N.set(%1N)", name)
            .build()
    )
    addFunction(
        FunSpec.builder(name)
            .addParameter(name, Provider::class.parameterizedBy(enumType))
            .addStatement("this.%1N.set(%1N)", name)
            .build()
    )
    addPropertiesForEnumValues(enumType)
}

internal fun TypeSpec.Builder.addPropertiesForEnumValues(enumType: KClass<out Enum<*>>) {
    enumType.java.enumConstants.iterator().forEach { enumValue ->
        val spec = PropertySpec.builder(enumValue.name, enumType).initializer("%T.%N", enumType, enumValue.name)
        enumValue::class.memberProperties
            .find { it.name == "description" && it.visibility != KVisibility.PRIVATE }
            ?.let {
                spec.addKdoc(it.getter.call(enumValue) as String)
            }
        addProperty(spec.build())
    }
}
