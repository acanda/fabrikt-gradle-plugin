package ch.acanda.gradle.fabrikt.build.builder

import ch.acanda.gradle.fabrikt.build.schema.ConfigurationDefinition
import ch.acanda.gradle.fabrikt.build.schema.ConfigurationSchema
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import org.gradle.api.Named
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

private const val CLASS_NAME_SUFFIX = "Extension"

/**
 * Builds the file FabriktExtensions.kt. This file contains the
 * extension classes for the `FabriktGenerateTask`.
 */
internal fun buildExtensions(schema: ConfigurationSchema): FileSpec {
    val builder = FileSpec.builder(PACKAGE, "FabriktExtensions")
    builder.addAnnotation(generated())
    schema.configurations.forEach { name, definition ->
        builder.addType(buildExtensions(ClassName(PACKAGE, "$name$CLASS_NAME_SUFFIX"), definition, schema))
    }
    return builder.build()
}

/**
 * Builds the extension class for a configuration.
 * ```kotlin
 * public abstract class GenerateTaskExtension @Inject constructor(objects: ObjectFactory) : Named {
 *   // properties
 *   public abstract val apiFragments: ConfigurableFileCollection
 *
 *   // nested extension classes
 *   @get:Nested
 *   public abstract val typeOverrides: TypeOverridesExtension
 *
 *   // polymorphic options
 *   @get:Internal
 *   public val Jakarta: PolymorphicJakartaOption =
 *       PolymorphicJakartaOption(ValidationLibraryOption.Jakarta)
 *
 *   // boolean values
 *   @get:Internal
 *   public val enabled: Boolean = true
 *   @get:Internal
 *   public val disabled: Boolean = false
 *
 *   // functions to configure nested extension classes
 *   public fun typeOverrides(action: Action<TypeOverridesExtension>) {
 *     action.execute(typeOverrides)
 *   }
 *
 *   // functions to add elements to list properties
 *   public fun customTypeMapping(action: Action<CustomTypeMappingExtension>): CustomTypeMappingExtension =
 *       objects.newInstance(CustomTypeMappingExtension::class.java).apply { action.execute(this) }
 * }
 * ```
 */
private fun buildExtensions(name: ClassName, config: ConfigurationDefinition, schema: ConfigurationSchema): TypeSpec {
    val spec = TypeSpec.classBuilder(name).addModifiers(KModifier.ABSTRACT)
    if (config.named) {
        spec.addSuperinterface(Named::class.asClassName())
        spec.primaryConstructor(FunSpec.constructorBuilder().addAnnotation(Inject::class).build())
    }
    val hasListType = config.properties.any { (_, property) -> isList(property.extensionType) }
    if (hasListType) {
        spec.primaryConstructor(
            FunSpec.constructorBuilder()
                .addAnnotation(Inject::class)
                .addParameter("objects", ObjectFactory::class)
                .build()
        )
        spec.addProperty(
            PropertySpec.builder("objects", ObjectFactory::class, KModifier.PRIVATE).initializer("objects").build()
        )
    }
    val optionTypes = config.properties
        .flatMap { (name, property) ->
            spec.addProperty(buildProperty(name, property.extensionType, schema, CLASS_NAME_SUFFIX))
            if (isNested(property.extensionType, schema.configurations)) {
                spec.addFunction(
                    FunSpec.builder(name)
                        .addParameter(
                            "action",
                            actionOf(getClassName(property.extensionType, schema, CLASS_NAME_SUFFIX))
                        )
                        .addCode("action.execute(%N)", name)
                        .build()
                )
            }
            if (isList(property.extensionType)) {
                val listType = getListType(property.extensionType)
                spec.addFunction(buildListElementConstructorFunction(listType, CLASS_NAME_SUFFIX))
            }
            if (isOption(property.extensionType, schema.options)) {
                listOf(property.type)
            } else {
                emptyList()
            }
        }
    spec.addProperties(
        schema.options
            .filter { (type, _) -> optionTypes.contains(type) }
            .buildPolymorphicOptions()
    )
    if (config.containsBooleanProperty()) {
        spec.addProperty(buildBooleanProperty("enabled", "true"))
        spec.addProperty(buildBooleanProperty("disabled", "false"))
    }
    return spec.build()
}
