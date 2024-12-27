package ch.acanda.gradle.fabrikt.build.builder

import ch.acanda.gradle.fabrikt.build.schema.ConfigurationDefinition
import ch.acanda.gradle.fabrikt.build.schema.ConfigurationSchema
import ch.acanda.gradle.fabrikt.build.schema.PropertyDefinition
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import org.gradle.api.file.ProjectLayout
import javax.inject.Inject
import kotlin.reflect.KClass

private const val CLASS_NAME_SUFFIX = "Defaults"

internal fun buildDefaults(schema: ConfigurationSchema): FileSpec {
    val builder = FileSpec.builder(PACKAGE, "FabriktExtensionDefaults")
    builder.addAnnotation(generated())
    schema.configurations.forEach { name, definition ->
        builder.addType(buildDefaults(ClassName(PACKAGE, "$name$CLASS_NAME_SUFFIX"), definition, schema))
    }
    return builder.build()
}

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
    config.properties
        .filter { (_, property) -> property.includeInDefaults == true }
        .map { (name, property) ->
            spec.addProperty(buildProperty(name, property, schema, CLASS_NAME_SUFFIX))
            if (property.isOption(schema.options)) {
                spec.addProperties(property.buildOptionProperties(schema.options))
            }
            if (property.isNested(schema.configurations)) {
                spec.addFunction(
                    FunSpec.builder(name)
                        .addParameter("action", actionOf(property.getClassName(schema, CLASS_NAME_SUFFIX)))
                        .addCode("action.execute(%N)", name)
                        .build()
                )
            }
        }
    if (config.containsBooleanProperty()) {
        spec.addProperty(PropertySpec.builder("enabled", Boolean::class).initializer("true").build())
        spec.addProperty(PropertySpec.builder("disabled", Boolean::class).initializer("false").build())
    }
    spec.addInitializerBlock(buildInitializerBlock(config))
    return spec.build()
}

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

