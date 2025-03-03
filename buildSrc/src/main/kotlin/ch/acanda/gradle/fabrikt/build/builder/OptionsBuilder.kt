package ch.acanda.gradle.fabrikt.build.builder

import ch.acanda.gradle.fabrikt.build.schema.OptionDefinition
import ch.acanda.gradle.fabrikt.build.schema.OptionDefinitions
import com.cjbooms.fabrikt.util.NormalisedString.pascalCase
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.WildcardTypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import org.gradle.api.provider.Property
import java.io.Serializable
import java.util.*
import kotlin.reflect.KClass

private const val OPTION_PARAM_NAME = "fabriktOption"

/**
 * Builds the file FabriktOptions.kt. This file contains the option types
 * holding the information to map the gradle plugin's options to their
 * respective Fabrikt options.
 */
internal fun buildOptions(options: OptionDefinitions): FileSpec {
    val builder = FileSpec.builder(PACKAGE, "FabriktOptions")
    builder.addAnnotation(generated())
    val fabriktOptionInterfaceName = ClassName(PACKAGE, "FabriktOption")
    builder.addType(buildOptionInterface(fabriktOptionInterfaceName))
    options.forEach { (name, definition) ->
        builder.addType(
            buildOption(ClassName(PACKAGE, name), fabriktOptionInterfaceName, definition)
        )
        val optionInterfaceName = ClassName(PACKAGE, "I$name")
        builder.addType(
            TypeSpec.interfaceBuilder(optionInterfaceName)
                .addModifiers(KModifier.SEALED)
                .addFunction(
                    FunSpec.builder("getOptionFor")
                        .addModifiers(KModifier.ABSTRACT)
                        .addParameter(
                            "type",
                            KClass::class.asClassName()
                                .parameterizedBy(WildcardTypeName.producerOf(fabriktOptionInterfaceName))
                        )
                        .returns(fabriktOptionInterfaceName)
                        .build()
                )
                .addType(
                    TypeSpec.companionObjectBuilder()
                        .addProperty(
                            PropertySpec
                                .builder(
                                    "options",
                                    List::class.asClassName().parameterizedBy(optionInterfaceName)
                                )
                                .initializer(
                                    "listOf(%L)",
                                    definition.mapping.keys.joinToString(", ") { key ->
                                        "${polymorphicOptionName(key).simpleName}(${name}.${key})"
                                    }
                                )
                                .build()
                        )
                        .build()
                )
                .build()
        )
        builder.addFunction(
            FunSpec.builder("withOptionName")
                .receiver(Property::class.asClassName().parameterizedBy(ClassName(PACKAGE, "I$name")))
                .addAnnotation(AnnotationSpec.builder(JvmName::class).addMember("\"withOptionName${name}\"").build())
                .addParameter(
                    ParameterSpec.builder(
                        "block",
                        LambdaTypeName.get(null, listOf(ParameterSpec.unnamed(String::class)), Unit::class.asTypeName())
                    ).build()
                )
                .addStatement(
                    """
                    |orNull?.let { option -> 
                    |  option.getOptionFor(%T::class).fabriktOption?.name?.let { block(it) }
                    |}
                    """.trimMargin(),
                    ClassName(PACKAGE, name)
                )
                .build()
        )
        builder.addProperty(
            PropertySpec.builder("option", ClassName(PACKAGE, name).nullable(true))
                .receiver(Property::class.asClassName().parameterizedBy(ClassName(PACKAGE, "I$name")))
                .addAnnotation(
                    AnnotationSpec.builder(JvmName::class)
                        .useSiteTarget(AnnotationSpec.UseSiteTarget.GET)
                        .addMember("\"option${name}\"")
                        .build()
                )
                .getter(
                    FunSpec.getterBuilder()
                        .addStatement(
                            """
                            |return orNull?.let { option -> 
                            |  option.getOptionFor(%1T::class) as %1T
                            |}
                            """.trimMargin(),
                            ClassName(PACKAGE, name)
                        )
                        .build()
                )
                .build()
        )
    }
    builder.addTypes(createPolymorphicOptions(options, fabriktOptionInterfaceName))
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
    val paramType = Class.forName(definition.source).asTypeName().nullable(definition)
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

private fun createPolymorphicOptions(
    options: OptionDefinitions,
    fabriktOptionInterfaceName: ClassName
) = options
    .flatMap { (option, definition) -> definition.mapping.keys.map { it to option } }
    .groupByTo(TreeMap(), { it.first }, { it.second })
    .map { (optionValue, options) ->
        TypeSpec.classBuilder(polymorphicOptionName(optionValue))
            .primaryConstructor(
                FunSpec
                    .constructorBuilder()
                    .addParameter("options", fabriktOptionInterfaceName, KModifier.VARARG)
                    .build()
            )
            .addSuperinterface(Serializable::class)
            .addSuperinterfaces(options.map { option -> ClassName(PACKAGE, "I$option") })
            .addProperty(
                PropertySpec
                    .builder("options", List::class.asClassName().parameterizedBy(fabriktOptionInterfaceName))
                    .initializer("listOf(*options)")
                    .build()
            )
            .addFunction(
                FunSpec
                    .builder("getOptionFor")
                    .addModifiers(KModifier.OVERRIDE)
                    .addParameter(
                        "type",
                        KClass::class.asClassName().parameterizedBy(
                            WildcardTypeName.producerOf(fabriktOptionInterfaceName)
                        )
                    )
                    .returns(fabriktOptionInterfaceName)
                    .addCode("return options.first { it::class == type }")
                    .build()
            )
            .build()
    }

