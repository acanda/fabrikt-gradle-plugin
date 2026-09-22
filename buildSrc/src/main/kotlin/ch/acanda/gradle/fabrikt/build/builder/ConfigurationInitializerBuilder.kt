package ch.acanda.gradle.fabrikt.build.builder

import ch.acanda.gradle.fabrikt.build.schema.ConfigurationDefinition
import ch.acanda.gradle.fabrikt.build.schema.ConfigurationSchema
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeAliasSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.WildcardTypeName.Companion.producerOf
import com.squareup.kotlinpoet.asClassName
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider

/**
 * Builds the file ConfigurationInitializer.kt. This file contains the
 * functions to initialize the configuration classes with the data from the
 * extension and defaults.
 */
internal fun buildInitializers(schema: ConfigurationSchema): FileSpec {
    val spec = FileSpec.builder(PACKAGE, "ConfigurationInitializer")
    spec.addAnnotation(generated())
    schema.configurations.forEach { name, config ->
        spec.addTypeAlias(initializerTypeAlias(name))
        spec.addFunction(buildInitializer(name, config, schema))
    }
    spec.addFunction(assignPropertyFunction())
    spec.addFunction(assignConfigurableFileCollectionFunction())
    spec.addFunction(assignListPropertyFunction())
    return spec.build()
}

/**
 * Builds the type alias `GenerateTaskConfigurationInitializer`.
 * ```kotlin
 * public typealias GenerateTaskConfigurationInitializer = GenerateTaskConfiguration.(source: GenerateTaskExtension, defaults: GenerateTaskDefaults) -> Unit
 * ```
 */
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

/**
 * Builds the funtion for initializing a configuration class, including its
 * nested configuration classes.
 * ```kotlin
 * internal fun initializeGenerateTaskConfiguration(`init`: GenerateTaskConfiguration.() -> Unit = {}): GenerateTaskConfigurationInitializer = { source, defaults ->
 *   apiFile.set(source.apiFile)
 *   apiFragments.assign(source.apiFragments, defaults.apiFragments)
 *   // initialize more properties...
 *   initializeTypeOverridesConfiguration().invoke(typeOverrides, source.typeOverrides, defaults.typeOverrides)
 *   initializeGenerateClientConfiguration().invoke(client, source.client, defaults.client)
 *   // initialize more nested classes...
 *   init(this)
 * }
 * ```
 */
private fun buildInitializer(name: String, config: ConfigurationDefinition, schema: ConfigurationSchema): FunSpec =
    FunSpec.builder("initialize${name}Configuration")
        .addModifiers(KModifier.INTERNAL)
        .addParameter("objects", ObjectFactory::class.java)
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
        if (isNested(property.type, schema.configurations)) {
            block.addStatement(
                "initialize%1NConfiguration(objects).invoke(%2N, source.%2N, defaults.%2N)", property.type, name
            )
        } else if (isList(property.type)) {
            val listType = getListType(property.type)
            fun mapping(source: String) = schema.configurations[listType]?.let { config ->
                config.properties.map { (name, _) -> "$name.set($source.$name)" }.joinToString("\n|    ")
            }
            block.addStatement(
                """
                |val %1NExtensionTransformer: (%2NExtension) -> %2NConfiguration = { extension ->
                |  objects.newInstance(%2NConfiguration::class.java).apply {
                |    ${mapping("extension")}
                |  }
                |}
                """.trimMargin(),
                name,
                listType
            )
            block.addStatement(
                """
                |val %1NDefaultsTransformer: (%2NDefaults) -> %2NConfiguration = { defaults -> 
                |  objects.newInstance(%2NConfiguration::class.java).apply {
                |    ${mapping("defaults")}
                |  }
                |}
                """.trimMargin(),
                name,
                listType
            )
            block.addStatement(
                "%1N.assign(source.%1N, defaults.%1N, %1NExtensionTransformer, %1NDefaultsTransformer)",
                name
            )
        } else {
            if (property.type == property.extensionType) {
                if (property.includeInDefaults) {
                    block.addStatement("%1N.assign(source.%1N, defaults.%1N)", name)
                } else {
                    block.addStatement("%1N.set(source.%1N)", name)
                }
            } else {
                val converterFunction = MemberName(
                    PACKAGE + ".converter",
                    "as${property.type.replace("<", "").replace(">", "")}"
                )
                if (property.includeInDefaults) {
                    block.addStatement("%1N.assign(source.%1N.%2M(), defaults.%1N.%2M())", name, converterFunction)
                } else {
                    block.addStatement("%1N.set(source.%1N.%2M())", name, converterFunction)
                }
            }
        }
    }
    block.addStatement("init(this)")
    block.endControlFlow()
    return block.build()
}

/**
 * Builds the function for assigning a property.
 * ```kotlin
 * private fun <T : Any> Property<T>.assign(`value`: Provider<out T>, defaultValue: Provider<out T>) {
 *   if (value.isPresent) { set(value.get()) } else { set(defaultValue.orNull) }
 * }
 * ```
 */
private fun assignPropertyFunction(): FunSpec =
    FunSpec.builder("assign")
        .addModifiers(KModifier.PRIVATE)
        .addTypeVariable(TypeVariableName("T", Any::class))
        .receiver(Property::class.asClassName().parameterizedBy(TypeVariableName("T")))
        .addParameter("value", Provider::class.asClassName().parameterizedBy(TypeVariableName("out T")))
        .addParameter("defaultValue", Provider::class.asClassName().parameterizedBy(TypeVariableName("out T")))
        .addStatement("if (value.isPresent) { set(value.get()) } else { set(defaultValue.orNull) }")
        .build()

/**
 * Builds the function for assigning a ConfigurableFileCollection.
 * ```kotlin
 * private fun ConfigurableFileCollection.assign(`value`: ConfigurableFileCollection, defaultValue: ConfigurableFileCollection) {
 *   if (!value.isEmpty) { setFrom(value) } else { setFrom(defaultValue) }
 * }
 * ```
 */
private fun assignConfigurableFileCollectionFunction(): FunSpec =
    FunSpec.builder("assign")
        .addModifiers(KModifier.PRIVATE)
        .receiver(ConfigurableFileCollection::class)
        .addParameter("value", ConfigurableFileCollection::class)
        .addParameter("defaultValue", ConfigurableFileCollection::class)
        .addStatement("if (!value.isEmpty) { setFrom(value) } else { setFrom(defaultValue) }")
        .build()

private fun assignListPropertyFunction(): FunSpec {
    val configTypeVariable = "CONFIG"
    val extensionTypeVariable = "EXTENSION"
    val defaultsTypeVariable = "DEFAULTS"
    val extensionTypeArguments =
        List::class.asClassName().parameterizedBy(TypeVariableName(extensionTypeVariable))
    val defaultsTypeArguments =
        List::class.asClassName().parameterizedBy(TypeVariableName(defaultsTypeVariable))
    return FunSpec.builder("assign")
        .addModifiers(KModifier.PRIVATE)
        .addTypeVariable(TypeVariableName(configTypeVariable, Any::class))
        .addTypeVariable(TypeVariableName(extensionTypeVariable, Any::class))
        .addTypeVariable(TypeVariableName(defaultsTypeVariable, Any::class))
        .receiver(ListProperty::class.asClassName().parameterizedBy(TypeVariableName(configTypeVariable)))
        .addParameter("value", Provider::class.asClassName().parameterizedBy(extensionTypeArguments))
        .addParameter("defaultValue", Provider::class.asClassName().parameterizedBy(defaultsTypeArguments))
        .addParameter(
            "extensionTransformer",
            LambdaTypeName.get(
                parameters = listOf(ParameterSpec("it", TypeVariableName(extensionTypeVariable))),
                returnType = TypeVariableName(configTypeVariable)
            )
        )
        .addParameter(
            "defaultsTransformer",
            LambdaTypeName.get(
                parameters = listOf(ParameterSpec("it", TypeVariableName(defaultsTypeVariable))),
                returnType = TypeVariableName(configTypeVariable)
            )
        )
        .addStatement(
            """
            val extension = value.orElse(emptyList<EXTENSION>()).map { e -> e.map(extensionTransformer) }
            val defaults = defaultValue.orElse(emptyList<DEFAULTS>()).map { e -> e.map(defaultsTransformer) }
            set(extension)
            addAll(defaults)
            """.trimIndent()
        )
        .build()
}
