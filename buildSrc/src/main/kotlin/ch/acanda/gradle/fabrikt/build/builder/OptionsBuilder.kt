package ch.acanda.gradle.fabrikt.build.builder

import ch.acanda.gradle.fabrikt.build.schema.OptionDefinition
import ch.acanda.gradle.fabrikt.build.schema.OptionDefinitions
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName

private const val OPTION_PARAM_NAME = "fabriktOption"

/**
 * Builds the file FabriktOptions.kt. This file contains the option types
 * holding the information to map the gradle plugin's options to their
 * respective Fabrikt options.
 */
internal fun buildOptions(packageName: String, options: OptionDefinitions): FileSpec {
    val builder = FileSpec.builder(packageName, "FabriktOptions")
    builder.addAnnotation(generated())
    val optionInterfaceName = ClassName(packageName, "FabriktOption")
    builder.addType(buildOptionInterface(optionInterfaceName))
    options.forEach { (name, definition) ->
        builder.addType(
            buildOption(ClassName(packageName, name), optionInterfaceName, definition)
        )
    }
    return builder.build()
}

private fun buildOptionInterface(name: ClassName) =
    TypeSpec
        .interfaceBuilder(name)
        .addModifiers(KModifier.SEALED)
        .addProperty(OPTION_PARAM_NAME, nullableEnumType)
        .build()

private val nullableEnumType =
    Enum::class.asTypeName().parameterizedBy(STAR).copy(nullable = true)

private fun buildOption(name: ClassName, optionInterface: ClassName, definition: OptionDefinition): TypeSpec {
    val paramType = Class.forName(definition.source)
        .asTypeName()
        .nullable(definition.mapping.any { (_, source) -> source == null })
    return TypeSpec
        .enumBuilder(name)
        .addSuperinterface(optionInterface)
        .primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameter(OPTION_PARAM_NAME, paramType)
                .build()
        )
        .addProperty(
            PropertySpec.builder(OPTION_PARAM_NAME, paramType, KModifier.OVERRIDE)
                .initializer(OPTION_PARAM_NAME)
                .build()
        )
        .addEnumConstants(definition)
        .build()
}

@Suppress("UNCHECKED_CAST")
private fun TypeSpec.Builder.addEnumConstants(definition: OptionDefinition) = this.apply {
    definition.mapping.forEach { (pluginName, fabriktName) ->
        addEnumConstant(pluginName, enumConstantSpec(definition.source, fabriktName))
    }
}

private fun enumConstantSpec(enumTypeName: String, enumConstantName: String?) =
    if (enumConstantName == null) {
        nullSpec
    } else {
        enumValueSpec(enumTypeName, enumConstantName)
    }

private val nullSpec =
    TypeSpec.anonymousClassBuilder().addSuperclassConstructorParameter("null").build()


private fun enumValueSpec(enumTypeName: String, enumConstantName: String): TypeSpec {
    val enumType = Class.forName(enumTypeName)
    require(enumType.isEnum) { "Type ${enumType.kotlin.qualifiedName} is not an enum class." }
    @Suppress("UNCHECKED_CAST")
    enumType as Class<Enum<*>>
    return TypeSpec.anonymousClassBuilder()
        .addSuperclassConstructorParameter("%T.%N", enumType, enumType[enumConstantName])
        .build()
}

@Suppress("UNCHECKED_CAST")
private operator fun Class<Enum<*>>.get(name: String): String =
    this.enumConstants.firstOrNull() { it.name == name }?.name
        ?: throw EnumConstantNotPresentException(this, name)
