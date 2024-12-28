package ch.acanda.gradle.fabrikt.build.builder

import ch.acanda.gradle.fabrikt.build.schema.ConfigurationDefinition
import ch.acanda.gradle.fabrikt.build.schema.ConfigurationSchema
import ch.acanda.gradle.fabrikt.build.schema.Dataflow
import ch.acanda.gradle.fabrikt.build.schema.PropertyDefinition
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.AnnotationSpec.UseSiteTarget.GET
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import org.gradle.api.Named
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.OutputFiles
import javax.inject.Inject

private const val CLASS_NAME_SUFFIX = "Configuration"

internal fun buildConfigurations(schema: ConfigurationSchema): FileSpec {
    val builder = FileSpec.builder(PACKAGE, "GenerateTaskConfiguration")
    builder.addAnnotation(generated())
    schema.configurations.forEach { name, definition ->
        builder.addType(buildConfigurations(ClassName(PACKAGE, "$name$CLASS_NAME_SUFFIX"), definition, schema))
    }
    return builder.build()
}

private fun buildConfigurations(
    name: ClassName,
    config: ConfigurationDefinition,
    schema: ConfigurationSchema
): TypeSpec {
    val spec = TypeSpec.classBuilder(name).addModifiers(KModifier.ABSTRACT)
    if (config.named) {
        spec.addSuperinterface(Named::class.asClassName())
        spec.primaryConstructor(
            FunSpec.constructorBuilder()
                .addAnnotation(Inject::class)
                .addParameter("name", String::class)
                .build()
        )
        spec.addProperty(PropertySpec.builder("name", String::class, KModifier.PRIVATE).initializer("name").build())
        spec.addFunction(
            FunSpec
                .builder("getName")
                .addAnnotation(Internal::class)
                .addModifiers(KModifier.OVERRIDE)
                .returns(String::class)
                .addCode("return name")
                .build()
        )
    }
    config.properties.map { (name, property) ->
        spec.addProperty(buildProperty(name, property, schema, CLASS_NAME_SUFFIX) {
            if (!property.isNested(schema.configurations)) {
                addAnnotation(AnnotationSpec.builder(property.dataflowAnnotation).useSiteTarget(GET).build())
            }
            if (property.isOptional()) {
                addAnnotation(AnnotationSpec.builder(Optional::class).useSiteTarget(GET).build())
            }
        })
        if (property.isOption(schema.options)) {
            spec.addProperties(
                property.buildOptionProperties(schema.options) {
                    internal()
                }
            )
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
        spec.addProperty(PropertySpec.builder("enabled", Boolean::class).internal().initializer("true").build())
        spec.addProperty(PropertySpec.builder("disabled", Boolean::class).internal().initializer("false").build())
    }
    return spec.build()
}

private val PropertyDefinition.dataflowAnnotation
    get() = when (type) {
        "RegularFileProperty" -> when (dataflow) {
            Dataflow.Input -> InputFile::class
            Dataflow.Output -> OutputFile::class
        }

        "ConfigurableFileCollection" -> when (dataflow) {
            Dataflow.Input -> InputFiles::class
            Dataflow.Output -> OutputFiles::class
        }

        "DirectoryProperty" -> when (dataflow) {
            Dataflow.Input -> InputDirectory::class
            Dataflow.Output -> OutputDirectory::class
        }

        else -> Input::class
    }

private fun PropertySpec.Builder.internal(): PropertySpec.Builder =
    addAnnotation(AnnotationSpec.builder(Internal::class).useSiteTarget(GET).build())

private fun PropertyDefinition.isOptional(): Boolean =
    mandatory == false
