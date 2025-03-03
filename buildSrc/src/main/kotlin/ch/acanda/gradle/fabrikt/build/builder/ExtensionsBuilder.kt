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
 * public abstract class GenerateTaskExtension @Inject constructor() : Named {
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
 * }
 *
 * ```
 */
private fun buildExtensions(name: ClassName, config: ConfigurationDefinition, schema: ConfigurationSchema): TypeSpec {
    val spec = TypeSpec.classBuilder(name).addModifiers(KModifier.ABSTRACT)
    if (config.named) {
        spec.addSuperinterface(Named::class.asClassName())
        spec.primaryConstructor(FunSpec.constructorBuilder().addAnnotation(Inject::class).build())
    }
    val optionTypes = config.properties.flatMap { (name, property) ->
        spec.addProperty(buildProperty(name, property, schema, CLASS_NAME_SUFFIX))
        if (property.isNested(schema.configurations)) {
            spec.addFunction(
                FunSpec.builder(name)
                    .addParameter("action", actionOf(property.getClassName(schema, CLASS_NAME_SUFFIX)))
                    .addCode("action.execute(%N)", name)
                    .build()
            )
        }
        if (property.isOption(schema.options)) {
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
