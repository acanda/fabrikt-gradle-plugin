package ch.acanda.gradle.fabrikt.build.builder

import ch.acanda.gradle.fabrikt.build.schema.ConfigurationDefinition
import ch.acanda.gradle.fabrikt.build.schema.ConfigurationDefinitions
import ch.acanda.gradle.fabrikt.build.schema.ConfigurationSchema
import ch.acanda.gradle.fabrikt.build.schema.OptionDefinitions
import ch.acanda.gradle.fabrikt.build.schema.PropertyDefinition
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.AnnotationSpec.UseSiteTarget.GET
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested
import javax.inject.Inject

private const val BOOLEAN = "Boolean"
private const val CHAR_SEQUENCE = "CharSequence"
private const val REGULAR_FILE_PROPERTY = "RegularFileProperty"
private const val CONFIGURABLE_FILE_COLLECTION = "ConfigurableFileCollection"
private const val DIRECTORY_PROPERTY = "DirectoryProperty"

internal fun buildExtensions(schema: ConfigurationSchema): FileSpec {
    val builder = FileSpec.builder(PACKAGE, "FabriktExtensions")
    builder.addAnnotation(generated())
    schema.configurations.forEach { name, definition ->
        builder.addType(buildExtension(ClassName(PACKAGE, "${name}Extension"), definition, schema))
    }
    return builder.build()
}

private fun buildExtension(name: ClassName, config: ConfigurationDefinition, schema: ConfigurationSchema): TypeSpec {
    val spec = TypeSpec.classBuilder(name)
        .addModifiers(KModifier.ABSTRACT)
    if (config.named) {
        spec.addSuperinterface(Named::class.asClassName())
        spec.addFunction(FunSpec.constructorBuilder().addAnnotation(Inject::class).build())
    }
    config.properties.map { (name, property) ->
        spec.addProperty(buildProperty(name, property, schema))
        if (property.isOption(schema.options)) {
            spec.addProperties(property.buildOptionProperties(schema.options))
        }
        if (property.isNested(schema.configurations)) {
            spec.addFunction(
                FunSpec.builder(name)
                    .addParameter("action", actionOf(property.getClassName(schema)))
                    .addCode("action.execute(%N)", name)
                    .build()
            )
        }
    }
    if (config.containsBooleanProperty()) {
        spec.addProperty(PropertySpec.builder("enabled", Boolean::class).initializer("true").build())
        spec.addProperty(PropertySpec.builder("disabled", Boolean::class).initializer("false").build())
    }
    return spec.build()
}

private fun buildProperty(name: String, property: PropertyDefinition, schema: ConfigurationSchema): PropertySpec =
    if (property.isNested(schema.configurations)) {
        buildNestedProperty(name, property, schema)
    } else {
        PropertySpec.builder(name, property.getPropertyType(schema), KModifier.ABSTRACT).build()
    }

private fun buildNestedProperty(name: String, property: PropertyDefinition, schema: ConfigurationSchema) =
    PropertySpec.builder(name, property.getClassName(schema), KModifier.ABSTRACT)
        .addAnnotation(nestedAnnotation)
        .build()


private val nestedAnnotation =
    AnnotationSpec.builder(Nested::class).useSiteTarget(GET).build()

private fun PropertyDefinition.getPropertyType(schema: ConfigurationSchema) =
    when (type) {
        REGULAR_FILE_PROPERTY, CONFIGURABLE_FILE_COLLECTION, DIRECTORY_PROPERTY -> getClassName(schema)
        else -> Property::class.asClassName().parameterizedBy(getClassName(schema))
    }

private fun PropertyDefinition.getClassName(schema: ConfigurationSchema) = when {
    type == BOOLEAN -> Boolean::class.asClassName()
    type == CHAR_SEQUENCE -> CharSequence::class.asClassName()
    type == REGULAR_FILE_PROPERTY -> RegularFileProperty::class.asClassName()
    type == CONFIGURABLE_FILE_COLLECTION -> ConfigurableFileCollection::class.asClassName()
    type == DIRECTORY_PROPERTY -> DirectoryProperty::class.asClassName()
    isOption(schema.options) -> ClassName(PACKAGE, type)
    isNested(schema.configurations) -> ClassName(PACKAGE, "${type}Extension")
    else -> throw IllegalArgumentException("Unknown property type: $type")
}

private fun PropertyDefinition.isOption(options: OptionDefinitions) =
    options.containsKey(type)

private fun PropertyDefinition.buildOptionProperties(options: OptionDefinitions): List<PropertySpec> =
    options[type]?.mapping.orEmpty().map { (name, _) -> buildOptionProperty(name, ClassName(PACKAGE, type)) }

private fun buildOptionProperty(name: String, type: TypeName) =
    PropertySpec.builder(name, type).initializer("%T.%N", type, name).build()

private fun PropertyDefinition.isNested(configs: ConfigurationDefinitions) =
    configs.containsKey(type)

private fun actionOf(type: TypeName) =
    Action::class.asClassName().parameterizedBy(type)

private fun ConfigurationDefinition.containsBooleanProperty() =
    properties.any { (_, property) -> property.type == BOOLEAN }
