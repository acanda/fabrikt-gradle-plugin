package ch.acanda.gradle.fabrikt.build.generator

import ch.acanda.gradle.fabrikt.build.ExtensionGenerator
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import org.gradle.api.provider.Property
import kotlin.reflect.KClass
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberProperties

internal fun <E> TypeSpec.Builder.enumProperty(name: String, enumType: KClass<out E>)
    where E : Enum<*>, E : FabriktOption = apply {
    val enumName = ClassName(ExtensionGenerator.PACKAGE, enumType.simpleName.orEmpty())
    addProperty(
        PropertySpec.builder(name, Property::class.asClassName().parameterizedBy(enumName))
            .initializer("%1N.property(%2T::class.java)", ExtensionGenerator.PROP_OBJECTS, enumName)
            .build()
    )
    addPropertiesForEnumValues(enumType, enumName)
}

internal fun <E> TypeSpec.Builder.addPropertiesForEnumValues(enumType: KClass<out E>, enumName: ClassName)
    where E : Enum<*>, E : FabriktOption {
    enumType.java.enumConstants.forEach { value ->
        val spec = PropertySpec.builder(value.name, enumName).initializer("%T.%N", enumName, value.name)
        value.fabriktOption?.let { option ->
            option::class.memberProperties
                .find { it.name == "description" && it.visibility != KVisibility.PRIVATE }
                ?.let {
                    spec.addKdoc(it.getter.call(option) as String)
                }
        }
        addProperty(spec.build())
    }
}
