package ch.acanda.gradle.fabrikt.build.builder

import ch.acanda.gradle.fabrikt.build.schema.ConfigurationDefinition
import ch.acanda.gradle.fabrikt.build.schema.ConfigurationSchema
import ch.acanda.gradle.fabrikt.build.schema.PropertyDefinition
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.AnnotationSpec.UseSiteTarget.GET
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import org.gradle.api.file.ProjectLayout
import org.gradle.api.tasks.Internal
import javax.inject.Inject
import kotlin.reflect.KClass

private const val CLASS_NAME_SUFFIX = "Defaults"

/**
 * Builds the file FabriktExtensionDefaults.kt. This file contains the
 * defaults classes for the `FabriktGenerateTask`. Those classes are almost
 * identical to the extension classes, but the properties are initialized with
 * their default values.
 */
internal fun buildDefaults(schema: ConfigurationSchema): FileSpec {
    val builder = FileSpec.builder(PACKAGE, "FabriktExtensionDefaults")
    builder.addAnnotation(generated())
    schema.configurations.forEach { name, definition ->
        builder.addType(buildDefaults(ClassName(PACKAGE, "$name$CLASS_NAME_SUFFIX"), definition, schema))
    }
    return builder.build()
}

/**
 * Builds the defaults class for a configuration.
 * ```kotlin
 * public abstract class GenerateTaskDefaults @Inject constructor(
 *   projectLayout: ProjectLayout,
 * ) {
 *   // properties
 *   public abstract val apiFragments: ConfigurableFileCollection
 *
 *   // nested defaults classes
 *   @get:Nested
 *   public abstract val typeOverrides: TypeOverridesDefaults
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
 *   // initializer block
 *   init {
 *     externalReferenceResolution.convention(targeted)
 *     outputDirectory.convention(projectLayout.buildDirectory.dir("generated/sources/fabrikt"))
 *   }
 *
 *   // functions to configure nested defaults classes
 *   public fun typeOverrides(action: Action<TypeOverridesDefaults>) {
 *     action.execute(typeOverrides)
 *   }
 *
 * }
 * ```
 */
private fun buildDefaults(name: ClassName, config: ConfigurationDefinition, schema: ConfigurationSchema): TypeSpec {
    val spec = TypeSpec.classBuilder(name).addModifiers(KModifier.ABSTRACT)
    if (config.injects.isNotEmpty()) {
        spec.primaryConstructor(
            config.injects
                .fold(FunSpec.constructorBuilder()) { builder, (name, type) ->
                    builder.addParameter(name, type)
                }
                .addAnnotation(Inject::class)
                .build()
        )
    }
    val optionTypes = config.properties
        .filter { (_, property) -> property.includeInDefaults == true }
        .flatMap { (name, property) ->
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
    spec.addInitializerBlock(buildInitializerBlock(config))
    return spec.build()
}

/**
 * Builds the initializer block for the defaults class. This block initilizes
 * the properties with their respective default value.
 * ```kotlin
 *   init {
 *     binary.convention(ByteArray)
 *     byte.convention(ByteArray)
 *     datetime.convention(OffsetDateTime)
 *   }
 * ```
 */
private fun buildInitializerBlock(config: ConfigurationDefinition): CodeBlock =
    config.properties.entries
        .filter { (_, property) -> property.default != null }
        .fold(CodeBlock.builder()) { builder, (name, property) ->
            val value = when {
                property.default == null -> null
                property.default.startsWith("ProjectLayout.") -> property.default.replaceFirstChar { it.lowercase() }
                else -> property.default
            }
            builder.addStatement(conventionTemplate(property), name, value)
        }
        .build()

private fun conventionTemplate(property: PropertyDefinition): String = when {
    property.type == CHAR_SEQUENCE -> "%N.convention(%S)"
    else -> "%N.convention(%L)"
}

private val ConfigurationDefinition.injects: Set<Pair<String, KClass<*>>>
    get() = properties.mapNotNull { (_, property) ->
        when {
            property.default?.startsWith("ProjectLayout.") == true -> "projectLayout" to ProjectLayout::class
            else -> null
        }
    }.toSet()
