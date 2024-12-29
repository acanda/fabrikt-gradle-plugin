package ch.acanda.gradle.fabrikt.build.builder

import ch.acanda.gradle.fabrikt.build.schema.ConfigurationDefinition
import ch.acanda.gradle.fabrikt.build.schema.ConfigurationSchema
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeAliasSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.asClassName
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider

internal fun buildInitializers(schema: ConfigurationSchema): FileSpec {
    val spec = FileSpec.builder(PACKAGE, "ConfigurationInitializer")
    spec.addAnnotation(generated())
    schema.configurations.forEach { name, config ->
        spec.addTypeAlias(initializerTypeAlias(name))
        spec.addFunction(buildInitializer(name, config, schema))
    }
    spec.addFunction(assignPropertyFunction())
    spec.addFunction(assignConfigurableFileCollectionFunction())
    return spec.build()
}

private fun initializerTypeAlias(name: String): TypeAliasSpec =
    TypeAliasSpec.builder(
        "${name}ConfigurationInitializer",
        LambdaTypeName.get(
            receiver = ClassName(PACKAGE, "${name}Configuration"),
            parameters = listOf(
                ParameterSpec("source", ClassName(PACKAGE, "${name}Extension")),
                ParameterSpec("defaults", ClassName(PACKAGE, "${name}Defaults"))
            ),
            returnType = UNIT
        )
    ).build()

private fun buildInitializer(name: String, config: ConfigurationDefinition, schema: ConfigurationSchema): FunSpec =
    FunSpec.builder("initialize${name}Configuration")
        .addModifiers(KModifier.INTERNAL)
        .addParameter(
            ParameterSpec
                .builder(
                    "init",
                    LambdaTypeName.get(
                        receiver = ClassName(PACKAGE, "${name}Configuration"),
                        parameters = emptyList(),
                        returnType = UNIT
                    )
                )
                .defaultValue(CodeBlock.of("{}"))
                .build()
        )
        .returns(ClassName(PACKAGE, "${name}ConfigurationInitializer"))
        .addCode(buildInitializerCodeBlock(config, schema))
        .build()


private fun buildInitializerCodeBlock(config: ConfigurationDefinition, schema: ConfigurationSchema): CodeBlock {
    val block = CodeBlock.builder()
    block.beginControlFlow("return { source, defaults ->")

    config.properties.forEach { (name, property) ->
        when {
            property.isNested(schema.configurations) ->
                block.addStatement(
                    "initialize%1NConfiguration().invoke(%2N, source.%2N, defaults.%2N)", property.type, name
                )

            !property.includeInDefaults -> block.addStatement("%1N.set(source.%1N)", name)
            else -> block.addStatement("%1N.assign(source.%1N, defaults.%1N)", name)
        }
    }

    block.addStatement("init(this)")
    block.endControlFlow()
    return block.build()
}

private fun assignPropertyFunction(): FunSpec =
    FunSpec.builder("assign")
        .addModifiers(KModifier.PRIVATE)
        .addTypeVariable(TypeVariableName("T"))
        .receiver(Property::class.asClassName().parameterizedBy(TypeVariableName("T")))
        .addParameter("value", Provider::class.asClassName().parameterizedBy(TypeVariableName("out T")))
        .addParameter("defaultValue", Provider::class.asClassName().parameterizedBy(TypeVariableName("out T")))
        .addStatement("if (value.isPresent) { set(value.get()) } else { set(defaultValue.orNull) }")
        .build()


private fun assignConfigurableFileCollectionFunction(): FunSpec =
    FunSpec.builder("assign")
        .addModifiers(KModifier.PRIVATE)
        .receiver(ConfigurableFileCollection::class)
        .addParameter("value", ConfigurableFileCollection::class)
        .addParameter("defaultValue", ConfigurableFileCollection::class)
        .addStatement("if (!value.isEmpty) { setFrom(value) } else { setFrom(defaultValue) }")
        .build()

