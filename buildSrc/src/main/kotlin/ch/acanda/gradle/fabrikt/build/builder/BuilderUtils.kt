package ch.acanda.gradle.fabrikt.build.builder

import ch.acanda.gradle.fabrikt.build.GeneratePluginClassesTask
import ch.acanda.gradle.fabrikt.build.schema.ConfigurationDefinition
import ch.acanda.gradle.fabrikt.build.schema.ConfigurationDefinitions
import ch.acanda.gradle.fabrikt.build.schema.ConfigurationSchema
import ch.acanda.gradle.fabrikt.build.schema.OptionDefinitions
import ch.acanda.gradle.fabrikt.build.schema.PropertyDefinition
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.AnnotationSpec.UseSiteTarget.GET
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import org.gradle.api.Action
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested
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
    nestedSuffix: String
): PropertySpec =
    if (property.isNested(schema.configurations)) {
        buildNestedProperty(name, property, schema, nestedSuffix)
    } else {
        PropertySpec.builder(name, property.getPropertyType(schema, nestedSuffix), KModifier.ABSTRACT).build()
    }

internal fun buildNestedProperty(
    name: String,
    property: PropertyDefinition,
    schema: ConfigurationSchema,
    nestedSuffix: String
): PropertySpec =
    PropertySpec.builder(name, property.getClassName(schema, nestedSuffix), KModifier.ABSTRACT)
        .addAnnotation(nestedAnnotation)
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
    isOption(schema.options) -> ClassName(PACKAGE, type)
    isNested(schema.configurations) -> ClassName(PACKAGE, "$type$nestedSuffix")
    else -> throw IllegalArgumentException("Unknown property type: $type")
}

internal fun PropertyDefinition.isOption(options: OptionDefinitions): Boolean =
    options.containsKey(type)

internal fun PropertyDefinition.buildOptionProperties(options: OptionDefinitions): List<PropertySpec> =
    options[type]?.mapping.orEmpty().map { (name, _) -> buildOptionProperty(name, ClassName(PACKAGE, type)) }

internal fun buildOptionProperty(name: String, type: TypeName): PropertySpec =
    PropertySpec.builder(name, type).initializer("%T.%N", type, name).build()

internal fun PropertyDefinition.isNested(configs: ConfigurationDefinitions): Boolean =
    configs.containsKey(type)

internal fun actionOf(type: TypeName): TypeName =
    Action::class.asClassName().parameterizedBy(type)

internal fun ConfigurationDefinition.containsBooleanProperty(): Boolean =
    properties.any { (_, property) -> property.type == BOOLEAN }
