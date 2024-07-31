package ch.acanda.gradle.fabrikt

import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import javax.inject.Inject

open class GenerateTaskConfiguration @Inject constructor(
    private val name: String,
    @get:Internal internal val project: Project
) : Named {

    @Internal
    override fun getName() = name

    @get:Internal
    val enabled: Boolean = true

    @get:Internal
    val disabled: Boolean = false

    @get:InputFile
    val apiFile: RegularFileProperty = project.objects.fileProperty()

    @get:InputFiles
    @get:Optional
    val apiFragments: ConfigurableFileCollection = project.objects.fileCollection()

    @get:Input
    @get:Optional
    val externalReferenceResolution: Property<ExternalReferencesResolutionOption> =
        project.objects.property(ExternalReferencesResolutionOption::class.java)

    @get:Internal
    val targeted: ExternalReferencesResolutionOption = ExternalReferencesResolutionOption.targeted

    @get:Internal
    val aggressive: ExternalReferencesResolutionOption = ExternalReferencesResolutionOption.aggressive

    @get:Input
    val basePackage: Property<CharSequence> = project.objects.property(CharSequence::class.java)

    @get:OutputDirectory
    @get:Optional
    val outputDirectory: DirectoryProperty = project.objects.directoryProperty()

    @get:Input
    @get:Optional
    val sourcesPath: Property<CharSequence> = project.objects.property(CharSequence::class.java)

    @get:Input
    @get:Optional
    val resourcesPath: Property<CharSequence> = project.objects.property(CharSequence::class.java)

    @get:Input
    @get:Optional
    val validationLibrary: Property<ValidationLibraryOption> =
        project.objects.property(ValidationLibraryOption::class.java)

    @get:Internal
    @Suppress("VariableNaming")
    val Jakarta: ValidationLibraryOption = ValidationLibraryOption.Jakarta

    @get:Internal
    @Suppress("VariableNaming")
    val Javax: ValidationLibraryOption = ValidationLibraryOption.Javax

    @get:Internal
    @Suppress("VariableNaming")
    val NoValidation: ValidationLibraryOption = ValidationLibraryOption.NoValidation

    @get:Input
    @get:Optional
    val quarkusReflectionConfig: Property<Boolean> = project.objects.property(Boolean::class.java)

    @get:Nested
    @get:Optional
    val typeOverrides: TypeOverridesConfiguration = project.objects.newInstance(TypeOverridesConfiguration::class.java)

    fun typeOverrides(action: Action<TypeOverridesConfiguration>) {
        action.execute(typeOverrides)
    }

    @get:Nested
    @get:Optional
    val client: GenerateClientConfiguration = project.objects.newInstance(GenerateClientConfiguration::class.java)

    fun client(action: Action<GenerateClientConfiguration>) {
        action.execute(client)
    }

    @get:Nested
    @get:Optional
    val controller: GenerateControllerConfiguration =
        project.objects.newInstance(GenerateControllerConfiguration::class.java)

    fun controller(action: Action<GenerateControllerConfiguration>) {
        action.execute(controller)
    }

    @get:Nested
    @get:Optional
    val model: GenerateModelConfiguration =
        project.objects.newInstance(GenerateModelConfiguration::class.java)

    fun model(action: Action<GenerateModelConfiguration>) {
        action.execute(model)
    }

    @get:Input
    @get:Optional
    open val skip: Property<Boolean> = project.objects.property(Boolean::class.java)

}

internal fun GenerateTaskConfiguration.copy(block: GenerateTaskConfiguration.() -> Unit = {}) =
    GenerateTaskConfiguration(name, project)
        .apply {
            apiFile.set(this@copy.apiFile)
            apiFragments.setFrom(this@copy.apiFragments)
            externalReferenceResolution.set(this@copy.externalReferenceResolution)
            basePackage.set(this@copy.basePackage)
            outputDirectory.set(this@copy.outputDirectory)
            sourcesPath.set(this@copy.sourcesPath)
            resourcesPath.set(this@copy.resourcesPath)
            validationLibrary.set(this@copy.validationLibrary)
            quarkusReflectionConfig.set(this@copy.quarkusReflectionConfig)
            @Suppress("UnnecessaryApply")
            typeOverrides.apply {
                datetime.set(this@copy.typeOverrides.datetime)
            }
            client.apply {
                generate.set(this@copy.client.generate)
                resilience4j.set(this@copy.client.resilience4j)
                suspendModifier.set(this@copy.client.suspendModifier)
                springResponseEntityWrapper.set(this@copy.client.springResponseEntityWrapper)
                target.set(this@copy.client.target)
            }
            controller.apply {
                generate.set(this@copy.controller.generate)
                authentication.set(this@copy.controller.authentication)
                suspendModifier.set(this@copy.controller.suspendModifier)
                completionStage.set(this@copy.controller.completionStage)
                target.set(this@copy.controller.target)
            }
            model.apply {
                generate.set(this@copy.model.generate)
                extensibleEnums.set(this@copy.model.extensibleEnums)
                javaSerialization.set(this@copy.model.javaSerialization)
                quarkusReflection.set(this@copy.model.quarkusReflection)
                micronautIntrospection.set(this@copy.model.micronautIntrospection)
                micronautReflection.set(this@copy.model.micronautReflection)
                includeCompanionObject.set(this@copy.model.includeCompanionObject)
                sealedInterfacesForOneOf.set(this@copy.model.sealedInterfacesForOneOf)
                nonNullMapValues.set(this@copy.model.nonNullMapValues)
                ignoreUnknownProperties.set(this@copy.model.ignoreUnknownProperties)
            }
            skip.set(this@copy.skip)
        }
        .apply(block)

open class TypeOverridesConfiguration @Inject constructor(objects: ObjectFactory) {

    @get:Input
    @get:Optional
    val datetime: Property<DateTimeOverrideOption> = objects.property(DateTimeOverrideOption::class.java)

    @get:Internal
    @Suppress("VariableNaming")
    val OffsetDateTime: DateTimeOverrideOption = DateTimeOverrideOption.OffsetDateTime

    @get:Internal
    @Suppress("VariableNaming")
    val Instant: DateTimeOverrideOption = DateTimeOverrideOption.Instant

    @get:Internal
    @Suppress("VariableNaming")
    val LocalDateTime: DateTimeOverrideOption = DateTimeOverrideOption.LocalDateTime

}

open class GenerateClientConfiguration @Inject constructor(objects: ObjectFactory) {

    @get:Input
    @get:Optional
    val generate: Property<Boolean> = objects.property(Boolean::class.java)

    @get:Input
    @get:Optional
    val resilience4j: Property<Boolean> = objects.property(Boolean::class.java)

    @get:Input
    @get:Optional
    val suspendModifier: Property<Boolean> = objects.property(Boolean::class.java)

    @get:Input
    @get:Optional
    val springResponseEntityWrapper: Property<Boolean> = objects.property(Boolean::class.java)

    @get:Input
    @get:Optional
    val target: Property<ClientTargetOption> = objects.property(ClientTargetOption::class.java)

    @get:Internal
    @Suppress("VariableNaming")
    val OkHttp: ClientTargetOption = ClientTargetOption.OkHttp

    @get:Internal
    @Suppress("VariableNaming")
    val OpenFeign: ClientTargetOption = ClientTargetOption.OpenFeign

}

open class GenerateControllerConfiguration @Inject constructor(objects: ObjectFactory) {

    @get:Input
    @get:Optional
    val generate: Property<Boolean> = objects.property(Boolean::class.java)

    @get:Input
    @get:Optional
    val authentication: Property<Boolean> = objects.property(Boolean::class.java)

    @get:Input
    @get:Optional
    val suspendModifier: Property<Boolean> = objects.property(Boolean::class.java)

    @get:Input
    @get:Optional
    val completionStage: Property<Boolean> = objects.property(Boolean::class.java)

    @get:Input
    @get:Optional
    val target: Property<ControllerTargetOption> = objects.property(ControllerTargetOption::class.java)

    @get:Internal
    @Suppress("VariableNaming")
    val Spring: ControllerTargetOption = ControllerTargetOption.Spring

    @get:Internal
    @Suppress("VariableNaming")
    val Micronaut: ControllerTargetOption = ControllerTargetOption.Micronaut

    @get:Internal
    @Suppress("VariableNaming")
    val Ktor: ControllerTargetOption = ControllerTargetOption.Ktor

}

open class GenerateModelConfiguration @Inject constructor(objects: ObjectFactory) {

    @get:Input
    @get:Optional
    val generate: Property<Boolean> = objects.property(Boolean::class.java)

    @get:Input
    @get:Optional
    val extensibleEnums: Property<Boolean> = objects.property(Boolean::class.java)

    @get:Input
    @get:Optional
    val javaSerialization: Property<Boolean> = objects.property(Boolean::class.java)

    @get:Input
    @get:Optional
    val quarkusReflection: Property<Boolean> = objects.property(Boolean::class.java)

    @get:Input
    @get:Optional
    val micronautIntrospection: Property<Boolean> = objects.property(Boolean::class.java)

    @get:Input
    @get:Optional
    val micronautReflection: Property<Boolean> = objects.property(Boolean::class.java)

    @get:Input
    @get:Optional
    val includeCompanionObject: Property<Boolean> = objects.property(Boolean::class.java)

    @get:Input
    @get:Optional
    val sealedInterfacesForOneOf: Property<Boolean> = objects.property(Boolean::class.java)

    @get:Input
    @get:Optional
    val nonNullMapValues: Property<Boolean> = objects.property(Boolean::class.java)

    @get:Input
    @get:Optional
    val ignoreUnknownProperties: Property<Boolean> = objects.property(Boolean::class.java)

}

open class GenerateTaskDefaults @Inject constructor(
    @get:Internal internal val project: Project
) {

    // apiFile and basePackage are meant to be different for each
    // configuration and therefore cannot have a default value.

    @get:Internal
    val enabled: Boolean = true

    @get:Internal
    val disabled: Boolean = false

    @get:InputFiles
    @get:Optional
    val apiFragments: ConfigurableFileCollection = project.objects.fileCollection()

    @get:Input
    @get:Optional
    val externalReferenceResolution: Property<ExternalReferencesResolutionOption> =
        project.objects.property(ExternalReferencesResolutionOption::class.java)
            .convention(ExternalReferencesResolutionOption.targeted)

    @get:Internal
    val targeted: ExternalReferencesResolutionOption = ExternalReferencesResolutionOption.targeted

    @get:Internal
    val aggressive: ExternalReferencesResolutionOption = ExternalReferencesResolutionOption.aggressive

    @get:OutputDirectory
    @get:Optional
    val outputDirectory: DirectoryProperty = project.objects.directoryProperty()
        .convention(project.layout.buildDirectory.dir("generated/sources/fabrikt"))

    @get:Input
    @get:Optional
    val sourcesPath: Property<CharSequence> = project.objects.property(CharSequence::class.java)
        .convention("src/main/kotlin")

    @get:Input
    @get:Optional
    val resourcesPath: Property<CharSequence> = project.objects.property(CharSequence::class.java)
        .convention("src/main/resources")

    @get:Input
    @get:Optional
    val validationLibrary: Property<ValidationLibraryOption> =
        project.objects.property(ValidationLibraryOption::class.java).convention(ValidationLibraryOption.Jakarta)

    @get:Internal
    @Suppress("VariableNaming")
    val Jakarta: ValidationLibraryOption = ValidationLibraryOption.Jakarta

    @get:Internal
    @Suppress("VariableNaming")
    val Javax: ValidationLibraryOption = ValidationLibraryOption.Javax

    @get:Internal
    @Suppress("VariableNaming")
    val NoValidation: ValidationLibraryOption = ValidationLibraryOption.NoValidation

    @get:Input
    @get:Optional
    val quarkusReflectionConfig: Property<Boolean> = project.objects.property(Boolean::class.java)
        .convention(false)

    @get:Nested
    @get:Optional
    val typeOverrides: TypeOverridesDefaults = project.objects.newInstance(TypeOverridesDefaults::class.java)

    fun typeOverrides(action: Action<TypeOverridesDefaults>) {
        action.execute(typeOverrides)
    }

    @get:Nested
    @get:Optional
    val client: GenerateClientDefaults = project.objects.newInstance(GenerateClientDefaults::class.java)

    fun client(action: Action<GenerateClientDefaults>) {
        action.execute(client)
    }

    @get:Nested
    @get:Optional
    val controller: GenerateControllerDefaults = project.objects.newInstance(GenerateControllerDefaults::class.java)

    fun controller(action: Action<GenerateControllerDefaults>) {
        action.execute(controller)
    }

    @get:Nested
    @get:Optional
    val model: GenerateModelDefaults =
        project.objects.newInstance(GenerateModelDefaults::class.java)

    fun model(action: Action<GenerateModelDefaults>) {
        action.execute(model)
    }

    @get:Input
    @get:Optional
    open val skip: Property<Boolean> = project.objects.property(Boolean::class.java)
        .convention(false)

}

open class TypeOverridesDefaults @Inject constructor(objects: ObjectFactory) {

    @get:Input
    @get:Optional
    val datetime: Property<DateTimeOverrideOption> =
        objects.property(DateTimeOverrideOption::class.java).convention(DateTimeOverrideOption.OffsetDateTime)

    @get:Internal
    @Suppress("VariableNaming")
    val OffsetDateTime: DateTimeOverrideOption = DateTimeOverrideOption.OffsetDateTime

    @get:Internal
    @Suppress("VariableNaming")
    val Instant: DateTimeOverrideOption = DateTimeOverrideOption.Instant

    @get:Internal
    @Suppress("VariableNaming")
    val LocalDateTime: DateTimeOverrideOption = DateTimeOverrideOption.LocalDateTime

}

open class GenerateClientDefaults @Inject constructor(objects: ObjectFactory) {

    @get:Input
    @get:Optional
    val generate: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

    @get:Input
    @get:Optional
    val resilience4j: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

    @get:Input
    @get:Optional
    val suspendModifier: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

    @get:Input
    @get:Optional
    val springResponseEntityWrapper: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

    @get:Input
    @get:Optional
    val target: Property<ClientTargetOption> = objects.property(ClientTargetOption::class.java)
        .convention(ClientTargetOption.OkHttp)

    @get:Internal
    @Suppress("VariableNaming")
    val OkHttp: ClientTargetOption = ClientTargetOption.OkHttp

    @get:Internal
    @Suppress("VariableNaming")
    val OpenFeign: ClientTargetOption = ClientTargetOption.OpenFeign

}

open class GenerateControllerDefaults @Inject constructor(objects: ObjectFactory) {

    @get:Input
    @get:Optional
    val generate: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

    @get:Input
    @get:Optional
    val authentication: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

    @get:Input
    @get:Optional
    val suspendModifier: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

    @get:Input
    @get:Optional
    val completionStage: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

    @get:Input
    @get:Optional
    val target: Property<ControllerTargetOption> = objects.property(ControllerTargetOption::class.java)
        .convention(ControllerTargetOption.Spring)

    @get:Internal
    @Suppress("VariableNaming")
    val Spring: ControllerTargetOption = ControllerTargetOption.Spring

    @get:Internal
    @Suppress("VariableNaming")
    val Micronaut: ControllerTargetOption = ControllerTargetOption.Micronaut

}

open class GenerateModelDefaults @Inject constructor(objects: ObjectFactory) {

    @get:Input
    @get:Optional
    val generate: Property<Boolean> = objects.property(Boolean::class.java).convention(true)

    @get:Input
    @get:Optional
    val extensibleEnums: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

    @get:Input
    @get:Optional
    val javaSerialization: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

    @get:Input
    @get:Optional
    val quarkusReflection: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

    @get:Input
    @get:Optional
    val micronautIntrospection: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

    @get:Input
    @get:Optional
    val micronautReflection: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

    @get:Input
    @get:Optional
    val includeCompanionObject: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

    @get:Input
    @get:Optional
    val sealedInterfacesForOneOf: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

    @get:Input
    @get:Optional
    val nonNullMapValues: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

    @get:Input
    @get:Optional
    val ignoreUnknownProperties: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

}

@Suppress("LongMethod")
internal fun GenerateTaskConfiguration.withDefaults(defaults: GenerateTaskDefaults) =
    GenerateTaskConfiguration(name, project).also { cfg ->
        cfg.apiFile.set(apiFile)
        cfg.apiFragments.setDefaultIfNotSet(apiFragments, defaults.apiFragments)
        cfg.basePackage.set(basePackage)
        cfg.externalReferenceResolution.setDefaultIfNotSet(
            externalReferenceResolution,
            defaults.externalReferenceResolution
        )
        cfg.outputDirectory.setDefaultIfNotSet(outputDirectory, defaults.outputDirectory)
        cfg.sourcesPath.setDefaultIfNotSet(sourcesPath, defaults.sourcesPath)
        cfg.resourcesPath.setDefaultIfNotSet(resourcesPath, defaults.resourcesPath)
        cfg.validationLibrary.setDefaultIfNotSet(validationLibrary, defaults.validationLibrary)
        cfg.quarkusReflectionConfig.setDefaultIfNotSet(quarkusReflectionConfig, defaults.quarkusReflectionConfig)
        cfg.typeOverrides.datetime.setDefaultIfNotSet(typeOverrides.datetime, defaults.typeOverrides.datetime)
        cfg.client.also {
            val clientDefaults = defaults.client
            it.generate.setDefaultIfNotSet(client.generate, clientDefaults.generate)
            it.resilience4j.setDefaultIfNotSet(client.resilience4j, clientDefaults.resilience4j)
            it.suspendModifier.setDefaultIfNotSet(client.suspendModifier, clientDefaults.suspendModifier)
            it.springResponseEntityWrapper.setDefaultIfNotSet(
                client.springResponseEntityWrapper,
                clientDefaults.springResponseEntityWrapper
            )
            it.target.setDefaultIfNotSet(client.target, clientDefaults.target)
        }
        cfg.controller.also {
            val controllerDefaults = defaults.controller
            it.generate.setDefaultIfNotSet(controller.generate, controllerDefaults.generate)
            it.authentication.setDefaultIfNotSet(controller.authentication, controllerDefaults.authentication)
            it.suspendModifier.setDefaultIfNotSet(controller.suspendModifier, controllerDefaults.suspendModifier)
            it.completionStage.setDefaultIfNotSet(controller.completionStage, controllerDefaults.completionStage)
            it.target.setDefaultIfNotSet(controller.target, controllerDefaults.target)
        }
        cfg.model.also {
            val modelDefaults = defaults.model
            it.generate.setDefaultIfNotSet(model.generate, modelDefaults.generate)
            it.extensibleEnums.setDefaultIfNotSet(model.extensibleEnums, modelDefaults.extensibleEnums)
            it.javaSerialization.setDefaultIfNotSet(model.javaSerialization, modelDefaults.javaSerialization)
            it.quarkusReflection.setDefaultIfNotSet(model.quarkusReflection, modelDefaults.quarkusReflection)
            it.micronautIntrospection.setDefaultIfNotSet(
                model.micronautIntrospection,
                modelDefaults.micronautIntrospection
            )
            it.micronautReflection.setDefaultIfNotSet(model.micronautReflection, modelDefaults.micronautReflection)
            it.includeCompanionObject.setDefaultIfNotSet(
                model.includeCompanionObject,
                modelDefaults.includeCompanionObject
            )
            it.sealedInterfacesForOneOf.setDefaultIfNotSet(
                model.sealedInterfacesForOneOf,
                modelDefaults.sealedInterfacesForOneOf
            )
            it.nonNullMapValues.setDefaultIfNotSet(model.nonNullMapValues, modelDefaults.nonNullMapValues)
            it.ignoreUnknownProperties.setDefaultIfNotSet(
                model.ignoreUnknownProperties,
                modelDefaults.ignoreUnknownProperties
            )
        }
        cfg.skip.setDefaultIfNotSet(skip, defaults.skip)
    }

private fun <T> Property<T>.setDefaultIfNotSet(value: Provider<out T>, defaultValue: Provider<out T>) {
    if (value.isPresent) {
        set(value)
    } else {
        set(defaultValue)
    }
}

private fun ConfigurableFileCollection.setDefaultIfNotSet(
    value: ConfigurableFileCollection,
    defaultValue: ConfigurableFileCollection
) {
    if (!value.isEmpty) {
        setFrom(value)
    } else {
        setFrom(defaultValue)
    }
}
