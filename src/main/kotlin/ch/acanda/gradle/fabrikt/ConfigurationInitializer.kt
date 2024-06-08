package ch.acanda.gradle.fabrikt

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider

internal fun initializeWithDefaults(
    init: GenerateTaskConfiguration.() -> Unit = {}
): GenerateTaskConfigurationInitializer = { source: GenerateTaskExtension, defaults ->
    apiFile.set(source.apiFile)
    apiFragments.assign(source.apiFragments, defaults.apiFragments)
    externalReferenceResolution.assign(source.externalReferenceResolution, defaults.externalReferenceResolution)
    basePackage.set(source.basePackage)
    outputDirectory.assign(source.outputDirectory, defaults.outputDirectory)
    sourcesPath.assign(source.sourcesPath, defaults.sourcesPath)
    resourcesPath.assign(source.resourcesPath, defaults.resourcesPath)
    validationLibrary.assign(source.validationLibrary, defaults.validationLibrary)
    // same as model.quarkusReflection?
    quarkusReflectionConfig.assign(source.quarkusReflectionConfig, defaults.quarkusReflectionConfig)
    typeOverrides.datetime.assign(source.typeOverrides.datetime, defaults.typeOverrides.datetime)
    with(client) {
        val s = source.client
        val d = defaults.client
        generate.assign(s.generate, d.generate)
        resilience4j.assign(s.resilience4j, d.resilience4j)
        suspendModifier.assign(s.suspendModifier, d.suspendModifier)
        springResponseEntityWrapper.assign(s.springResponseEntityWrapper, d.springResponseEntityWrapper)
        target.assign(s.target, d.target)
    }
    with(controller) {
        val s = source.controller
        val d = defaults.controller
        generate.assign(s.generate, d.generate)
        authentication.assign(s.authentication, d.authentication)
        suspendModifier.assign(s.suspendModifier, d.suspendModifier)
        completionStage.assign(s.completionStage, d.completionStage)
        target.assign(s.target, d.target)
    }
    with(model) {
        val s = source.model
        val d = defaults.model
        generate.assign(s.generate, d.generate)
        extensibleEnums.assign(s.extensibleEnums, d.extensibleEnums)
        javaSerialization.assign(s.javaSerialization, d.extensibleEnums)
        quarkusReflection.assign(s.quarkusReflection, d.quarkusReflection)
        micronautIntrospection.assign(s.micronautIntrospection, d.micronautIntrospection)
        micronautReflection.assign(s.micronautReflection, d.micronautReflection)
        includeCompanionObject.assign(s.includeCompanionObject, d.includeCompanionObject)
        sealedInterfacesForOneOf.assign(s.sealedInterfacesForOneOf, d.sealedInterfacesForOneOf)
        nonNullMapValues.assign(s.nonNullMapValues, d.nonNullMapValues)
        ignoreUnknownProperties.assign(s.ignoreUnknownProperties, d.ignoreUnknownProperties)
    }
    skip.assign(source.skip, defaults.skip)
    init.invoke(this)
}

private fun <T> Property<T>.assign(value: Provider<out T>, defaultValue: Provider<out T>) {
    if (value.isPresent) {
        set(value.get())
    } else {
        set(defaultValue.orNull)
    }
}

private fun ConfigurableFileCollection.assign(
    value: ConfigurableFileCollection,
    defaultValue: ConfigurableFileCollection
) {
    if (!value.isEmpty) {
        setFrom(value)
    } else {
        setFrom(defaultValue)
    }
}
