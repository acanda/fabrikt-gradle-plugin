package ch.acanda.gradle.fabrikt.build.builder

import ch.acanda.gradle.fabrikt.build.schema.Deprecation
import ch.acanda.gradle.fabrikt.build.schema.OptionDefinition
import ch.acanda.gradle.fabrikt.build.schema.OptionDefinitions
import ch.acanda.gradle.fabrikt.build.schema.OptionMappingValue
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.AnnotationSpec.UseSiteTarget.GET
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.WildcardTypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import org.gradle.api.logging.Logging
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
    val fabriktOptionInterfaceName = ClassName(PACKAGE, "FabriktOption")
    val builder = FileSpec.builder(PACKAGE, "FabriktOptions")
        .addAnnotation(generated())
        .addType(buildOptionInterface(fabriktOptionInterfaceName))

    options.forEach { (name, definition) ->
        builder.addType(buildOption(ClassName(PACKAGE, name), fabriktOptionInterfaceName, definition))
        val optionInterfaceName = ClassName(PACKAGE, "I$name")
        builder.addType(
            buildPolymorphicOptionInterface(optionInterfaceName, fabriktOptionInterfaceName, definition, name)
        )
        builder.addFunction(
            FunSpec.builder("withOptionName")
                .receiver(Property::class.asClassName().parameterizedBy(ClassName(PACKAGE, "I$name")))
                .addAnnotation(jvmName("withOptionName${name}"))
                .addParameter(
                    ParameterSpec.builder(
                        "block",
                        LambdaTypeName.get(null, listOf(ParameterSpec.unnamed(String::class)), Unit::class.asTypeName())
                    ).build()
                )
                .addStatement(
                    """
                    |option?.fabriktOption?.name?.let { block(it) }
                    """.trimMargin(),
                    ClassName(PACKAGE, name)
                )
                .build()
        )
        builder.addProperty(buildExtensionPropertyOption(name))
    }
    builder.addTypes(buildPolymorphicOptions(options, fabriktOptionInterfaceName))
    return builder.build()
}

/**
 * Builds the extension property `option` for a Gradle property holding a
 * polymorphic option. This property is used in tests.
 *
 * ```kotlin
 * @get:JvmName("optionBinaryOverrideOption")
 * public val Property<IBinaryOverrideOption>.option: BinaryOverrideOption?
 *   get() = orNull?.let { option ->
 *     option.getOptionFor(BinaryOverrideOption::class) as BinaryOverrideOption
 *   }
 * ```
 */
private fun buildExtensionPropertyOption(name: String) =
    PropertySpec.builder("option", ClassName(PACKAGE, name).nullable(true))
        .receiver(Property::class.asClassName().parameterizedBy(ClassName(PACKAGE, "I$name")))
        .addAnnotation(jvmName("option${name}", GET))
        .getter(
            FunSpec.getterBuilder()
                .addStatement(
                    """
                    |return orNull?.let { polyOption -> 
                    |  (polyOption.getOptionFor(%1T::class) as %1T)
                    |  .also { option ->
                    |    option::class.java.getField(option.name).getAnnotation(%2T::class.java)?.also { annotation -> 
                    |      %3T.getLogger(%1T::class.java).warn(annotation.message)
                    |    }
                    |  }
                    |}
                    """.trimMargin(),
                    ClassName(PACKAGE, name),
                    Deprecated::class,
                    Logging::class
                )
                .build()
        )
        .build()

/**
 * Builds the interface for the polymorphic option.

 * ```kotlin
 * public sealed interface IBinaryOverrideOption {
 *   public fun getOptionFor(type: KClass<out FabriktOption>): FabriktOption
 *
 *   public companion object {
 *     public val options: List<IBinaryOverrideOption> =
 *         listOf(
 *           PolymorphicByteArrayOption(BinaryOverrideOption.ByteArray),
 *           PolymorphicInputStreamOption(BinaryOverrideOption.InputStream)
 *         )
 *   }
 * }
 * ```
 */
private fun buildPolymorphicOptionInterface(
    optionInterfaceName: ClassName,
    fabriktOptionInterfaceName: ClassName,
    definition: OptionDefinition,
    name: String
) =
    TypeSpec.interfaceBuilder(optionInterfaceName)
        .addModifiers(KModifier.SEALED)
        .addFunction(buildGetOptionForFunction(fabriktOptionInterfaceName))
        .addType(buildCompanionForPolymorphicOptionInterface(optionInterfaceName, definition, name))
        .build()

/**
 * Builds the `getOptionFor` function for the polymorphic option interface.
 * ```kotlin
 * public fun getOptionFor(type: KClass<out FabriktOption>): FabriktOption
 * ```
 */
private fun buildGetOptionForFunction(fabriktOptionInterfaceName: ClassName) =
    FunSpec.builder("getOptionFor")
        .addModifiers(KModifier.ABSTRACT)
        .addParameter(
            "type",
            KClass::class.asClassName()
                .parameterizedBy(WildcardTypeName.producerOf(fabriktOptionInterfaceName))
        )
        .returns(fabriktOptionInterfaceName)
        .build()

/**
 * Builds the companion object for the polymorphic option interface.
 * ```kotlin
 * public companion object {
 *   public val options: List<IBinaryOverrideOption> =
 *       listOf(
 *         PolymorphicByteArrayOption(BinaryOverrideOption.ByteArray),
 *         PolymorphicInputStreamOption(BinaryOverrideOption.InputStream)
 *       )
 * }
 * ```
 */
private fun buildCompanionForPolymorphicOptionInterface(
    optionInterfaceName: ClassName,
    definition: OptionDefinition,
    name: String
) =
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

/**
 * Builds the `FabriktOption` interface.
 *
 * ```kotlin
 * public sealed interface FabriktOption {
 *   public val fabriktOption: Enum<*>?
 * }
 * ```
 */
private fun buildOptionInterface(name: ClassName) =
    TypeSpec
        .interfaceBuilder(name)
        .addModifiers(KModifier.SEALED)
        .addProperty(OPTION_PARAM_NAME, nullableEnumType)
        .build()

/** Type `Enum<*>?` */
private val nullableEnumType: TypeName =
    Enum::class.asTypeName().parameterizedBy(STAR).copy(nullable = true)

/**
 * Builds the enum class for an option.
 *
 * ```kotlin
 * public enum class BinaryOverrideOption(
 *   override val fabriktOption: CodeGenTypeOverride?,
 * ) : FabriktOption {
 *   ByteArray(null),
 *   InputStream(CodeGenTypeOverride.BYTEARRAY_AS_INPUTSTREAM)
 * }
 * ```
 */
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
    definition.mapping.forEach { (optionName, fabriktName) ->
        addEnumConstant(optionName, enumConstantSpec(optionName,definition.source, fabriktName))
    }
}

private fun enumConstantSpec(optionName: String, enumTypeName: String, enumConstant: OptionMappingValue?): TypeSpec {
    val spec = if (enumConstant == null || enumConstant.value == null) {
        nullSpec
    } else {
        enumValueSpec(enumTypeName, enumConstant.value)
    }
    if (enumConstant?.deprecated != null) {
        spec.addAnnotation(enumTypeName, optionName, enumConstant.deprecated)
    }
    return spec.build()
}

private val nullSpec: TypeSpec.Builder =
    TypeSpec.anonymousClassBuilder().addSuperclassConstructorParameter("null")


private fun enumValueSpec(enumTypeName: String, enumConstantName: String): TypeSpec.Builder {
    val enumType = Class.forName(enumTypeName)
    require(enumType.isEnum) { "Type ${enumType.kotlin.qualifiedName} is not an enum class." }
    @Suppress("UNCHECKED_CAST")
    enumType as Class<Enum<*>>
    return TypeSpec.anonymousClassBuilder()
        .addSuperclassConstructorParameter("%T.%N", enumType, enumType[enumConstantName])
}

private fun TypeSpec.Builder.addAnnotation(enumTypeName: String, enumValueName: String, deprecation: Deprecation) =
    addAnnotation(
        AnnotationSpec.builder(Deprecated::class)
            .addMember("message = %S", "`$enumTypeName.$enumValueName` is deprecated. Use `${deprecation.replaceWith}` instead.")
            .addMember(
                "replaceWith = ReplaceWith(%S, imports = [%S])",
                deprecation.replaceWith,
                "$enumTypeName.${deprecation.replaceWith}"
            )
            .build()
    )

@Suppress("UNCHECKED_CAST")
private operator fun Class<Enum<*>>.get(name: String): String =
    this.enumConstants.firstOrNull() { it.name == name }?.name
        ?: throw EnumConstantNotPresentException(this, name)

/**
 * Builds the class for a polymorphic option.
 *
 * ```kotlin
 * public class PolymorphicByteArrayOption(
 *   vararg options: FabriktOption,
 * ) : Serializable, IBinaryOverrideOption, IByteOverrideOption {
 *   public val options: List<FabriktOption> = listOf(*options)
 *
 *   override fun getOptionFor(type: KClass<out FabriktOption>): FabriktOption = options.first { it::class == type }
 * }
 * ```
 */
private fun buildPolymorphicOptions(
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

