package ch.acanda.gradle.fabrikt.build.builder

import ch.acanda.gradle.fabrikt.build.schema.ConfigurationSchema
import ch.acanda.gradle.fabrikt.build.schema.ValueDefinition
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import java.io.Serializable

internal fun buildValues(schema: ConfigurationSchema): FileSpec {
    val file = FileSpec.builder(PACKAGE, "FabriktValues").addAnnotation(generated())
    schema.values.forEach { (name, definition) ->
        file.addType(buildValue(name, definition))
        file.addType(buildBuilder(name, definition))
    }
    return file.build()
}

private fun buildValue(name: String, definition: ValueDefinition): TypeSpec {
    val constructor = FunSpec.constructorBuilder()
    val type = TypeSpec.classBuilder(name)
        .addModifiers(KModifier.DATA)
        .addSuperinterface(Serializable::class.asClassName())
    definition.properties.forEach { (propertyName, property) ->
        val parameter = ParameterSpec.builder(propertyName, STRING.copy(nullable = !property.mandatory)).apply {
            if (!property.mandatory) defaultValue("null")
        }.build()
        constructor.addParameter(parameter)
        type.addProperty(
            PropertySpec.builder(propertyName, STRING.copy(nullable = !property.mandatory))
                .initializer(propertyName)
                .build()
        )
    }
    return type.primaryConstructor(constructor.build()).build()
}

private fun buildBuilder(name: String, definition: ValueDefinition): TypeSpec {
    val builder = TypeSpec.classBuilder("${name}Builder")
    definition.properties.forEach { (propertyName, _) ->
        builder.addProperty(
            PropertySpec.builder(propertyName, STRING.copy(nullable = true), KModifier.PUBLIC)
                .mutable(true)
                .initializer("null")
                .build()
        )
    }
    val arguments = definition.properties.map { (propertyName, property) ->
        if (property.mandatory) "requireNotNull($propertyName)" else propertyName
    }.joinToString(", ")
    builder.addFunction(
        FunSpec.builder("build")
            .addModifiers(KModifier.INTERNAL)
            .returns(ClassName(PACKAGE, name))
            .addStatement("return %T($arguments)", ClassName(PACKAGE, name))
            .build()
    )
    return builder.build()
}
