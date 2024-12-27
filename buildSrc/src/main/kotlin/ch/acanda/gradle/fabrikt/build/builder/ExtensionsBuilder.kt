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

internal fun buildExtensions(schema: ConfigurationSchema): FileSpec {
    val builder = FileSpec.builder(PACKAGE, "FabriktExtensions")
    builder.addAnnotation(generated())
    schema.configurations.forEach { name, definition ->
        builder.addType(buildDefaults(ClassName(PACKAGE, "$name$CLASS_NAME_SUFFIX"), definition, schema))
    }
    return builder.build()
}

private fun buildDefaults(name: ClassName, config: ConfigurationDefinition, schema: ConfigurationSchema): TypeSpec {
    val spec = TypeSpec.classBuilder(name).addModifiers(KModifier.ABSTRACT)
    if (config.named) {
        spec.addSuperinterface(Named::class.asClassName())
        spec.primaryConstructor(FunSpec.constructorBuilder().addAnnotation(Inject::class).build())
    }
    config.properties.map { (name, property) ->
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
    return spec.build()
}
