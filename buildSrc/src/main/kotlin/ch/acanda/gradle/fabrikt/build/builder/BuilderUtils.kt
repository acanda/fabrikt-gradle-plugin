package ch.acanda.gradle.fabrikt.build.builder

import ch.acanda.gradle.fabrikt.build.GeneratePluginClassesTask
import ch.acanda.gradle.fabrikt.build.schema.ConfigurationDefinition
import ch.acanda.gradle.fabrikt.build.schema.ConfigurationDefinitions
import ch.acanda.gradle.fabrikt.build.schema.ConfigurationSchema
import ch.acanda.gradle.fabrikt.build.schema.OptionDefinition
import ch.acanda.gradle.fabrikt.build.schema.OptionDefinitions
import ch.acanda.gradle.fabrikt.build.schema.PropertyDefinition
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.AnnotationSpec.UseSiteTarget.GET
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import org.gradle.api.Action
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import java.util.*
import javax.annotation.processing.Generated

internal const val PACKAGE = "ch.acanda.gradle.fabrikt"
internal const val BOOLEAN = "Boolean"
internal const val CHAR_SEQUENCE = "CharSequence"
internal const val REGULAR_FILE_PROPERTY = "RegularFileProperty"
internal const val CONFIGURABLE_FILE_COLLECTION = "ConfigurableFileCollection"
internal const val DIRECTORY_PROPERTY = "DirectoryProperty"

internal fun generated() = AnnotationSpec.builder(Generated::class)
    .addMember("\"${GeneratePluginClassesTask::class.qualifiedName}\"")
    .build()

internal fun TypeName.nullable(definition: OptionDefinition): TypeName =
    nullable(definition.mapping.any { (_, source) -> source == null })

internal fun TypeName.nullable(nullable: Boolean = true) =
    if (nullable) {
        copy(nullable = true)
    } else {
        this
    }

internal fun buildProperty(
    name: String,
    property: PropertyDefinition,
    schema: ConfigurationSchema,
    nestedSuffix: String,
    init: PropertySpec.Builder.() -> Unit = {}
): PropertySpec =
    if (property.isNested(schema.configurations)) {
        buildNestedProperty(name, property, schema, nestedSuffix, init)
    } else {
        PropertySpec
            .builder(name, property.getPropertyType(schema, nestedSuffix), KModifier.ABSTRACT)
            .apply { init() }
            .build()
    }

internal fun buildNestedProperty(
    name: String,
    property: PropertyDefinition,
    schema: ConfigurationSchema,
    nestedSuffix: String,
    init: PropertySpec.Builder.() -> Unit
): PropertySpec =
    PropertySpec.builder(name, property.getClassName(schema, nestedSuffix), KModifier.ABSTRACT)
        .addAnnotation(nestedAnnotation)
        .apply { init() }
        .build()

internal val nestedAnnotation =
    AnnotationSpec.builder(Nested::class).useSiteTarget(GET).build()

internal fun PropertyDefinition.getPropertyType(schema: ConfigurationSchema, nestedSuffix: String): TypeName =
    when (type) {
        REGULAR_FILE_PROPERTY, CONFIGURABLE_FILE_COLLECTION, DIRECTORY_PROPERTY -> getClassName(schema, nestedSuffix)
        else -> Property::class.asClassName().parameterizedBy(getClassName(schema, nestedSuffix))
    }

internal fun PropertyDefinition.getClassName(schema: ConfigurationSchema, nestedSuffix: String): ClassName = when {
    type == BOOLEAN -> Boolean::class.asClassName()
    type == CHAR_SEQUENCE -> CharSequence::class.asClassName()
    type == REGULAR_FILE_PROPERTY -> RegularFileProperty::class.asClassName()
    type == CONFIGURABLE_FILE_COLLECTION -> ConfigurableFileCollection::class.asClassName()
    type == DIRECTORY_PROPERTY -> DirectoryProperty::class.asClassName()
    isOption(schema.options) -> ClassName(PACKAGE, "I$type")
    isNested(schema.configurations) -> ClassName(PACKAGE, "$type$nestedSuffix")
    else -> throw IllegalArgumentException("Unknown property type: $type")
}

internal fun PropertyDefinition.isOption(options: OptionDefinitions): Boolean =
    options.containsKey(type)

internal fun PropertyDefinition.buildOptionProperties(
    options: OptionDefinitions,
    init: PropertySpec.Builder.() -> Unit = {}
): List<PropertySpec> =
    options[type]?.mapping.orEmpty()
        .map { (name, _) -> buildOptionProperty(name, polymorphicOptionName(name), init) }

internal fun buildOptionProperty(name: String, type: TypeName, init: PropertySpec.Builder.() -> Unit): PropertySpec =
    PropertySpec.builder(name, type)
        .initializer("%T(X.%N)", type, name)
        .apply { init() }.build()

internal fun PropertyDefinition.isNested(configs: ConfigurationDefinitions): Boolean =
    configs.containsKey(type)

internal fun actionOf(type: TypeName): TypeName =
    Action::class.asClassName().parameterizedBy(type)

internal fun ConfigurationDefinition.containsBooleanProperty(): Boolean =
    properties.any { (_, property) -> property.type == BOOLEAN }

internal fun FileSpec.Builder.addTypes(types: Iterable<TypeSpec>) =
    types.forEach { this.addType(it) }

internal fun Map<String, OptionDefinition>.buildPolymorphicOptions(): List<PropertySpec> =
    flatMap { (option, definition) -> definition.mapping.keys.map { it to option } }
        .groupByTo(TreeMap(), { it.first }, { it.second })
        .map { (optionValue, options) -> buildPolymorphicOption(optionValue, options) }

/**
 * Builds a polymorphic option property that can be used to set the value of an option.
 * ```kotlin
 *   @JvmField
 *   public val ByteArray: PolymorphicByteArrayOption =
 *       PolymorphicByteArrayOption(BinaryOverrideOption.ByteArray, ByteOverrideOption.ByteArray)
 * ```
 */
private fun buildPolymorphicOption(optionValue: String, options: Iterable<String>): PropertySpec =
    PropertySpec
        .builder(optionValue, polymorphicOptionName(optionValue))
        .addAnnotation(AnnotationSpec.builder(JvmField::class).build())
        .initializer(
            "%T(%L)",
            polymorphicOptionName(optionValue),
            options.joinToString(", ") { "${it}.$optionValue" }
        )
        .build()

internal fun polymorphicOptionName(optionValue: String): ClassName =
    ClassName(PACKAGE, "Polymorphic${optionValue.pascalCase()}Option")

internal fun String.pascalCase(): String =
    replaceFirstChar { it.uppercase() }

/**
 * ```kotlin
 * @get:JvmName("optionBinaryOverrideOption")
 * ```
 */
internal fun jvmName(name: String, target: AnnotationSpec.UseSiteTarget? = null): AnnotationSpec =
    AnnotationSpec.builder(JvmName::class)
        .addMember("%S", name)
        .useSiteTarget(target)
        .build()

/**
 * Builds a boolean property with the given name, initializer and @JvmField.
 * ```kotlin
 * @JvmField
 * public val enabled: Boolean = true
 * ```
 */
internal fun buildBooleanProperty(name: String, initializer: String) =
    PropertySpec.builder(name, Boolean::class)
        .addAnnotation(AnnotationSpec.builder(JvmField::class).build())
        .initializer(initializer)
        .build()
