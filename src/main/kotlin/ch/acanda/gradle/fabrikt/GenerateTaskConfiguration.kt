package ch.acanda.gradle.fabrikt

import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
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
    @get:Internal internal val objects: ObjectFactory,
) : Named {

    @Internal
    override fun getName() = name

    @get:Internal
    val enabled: Boolean = true

    @get:Internal
    val disabled: Boolean = false

    @get:InputFile
    val apiFile: RegularFileProperty = objects.fileProperty()

    @get:InputFiles
    @get:Optional
    val apiFragments: ConfigurableFileCollection = objects.fileCollection()

    @get:Input
    @get:Optional
    val externalReferenceResolution: Property<ExternalReferencesResolutionOption> =
        objects.property(ExternalReferencesResolutionOption::class.java)

    @get:Internal
    val targeted: ExternalReferencesResolutionOption = ExternalReferencesResolutionOption.targeted

    @get:Internal
    val aggressive: ExternalReferencesResolutionOption = ExternalReferencesResolutionOption.aggressive

    @get:Input
    val basePackage: Property<CharSequence> = objects.property(CharSequence::class.java)

    @get:OutputDirectory
    @get:Optional
    val outputDirectory: DirectoryProperty = objects.directoryProperty()

    @get:Input
    @get:Optional
    val sourcesPath: Property<CharSequence> = objects.property(CharSequence::class.java)

    @get:Input
    @get:Optional
    val resourcesPath: Property<CharSequence> = objects.property(CharSequence::class.java)

    @get:Input
    @get:Optional
    val validationLibrary: Property<ValidationLibraryOption> =
        objects.property(ValidationLibraryOption::class.java)

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
    val quarkusReflectionConfig: Property<Boolean> = objects.property(Boolean::class.java)

    @get:Nested
    @get:Optional
    val typeOverrides: TypeOverridesConfiguration = objects.newInstance(TypeOverridesConfiguration::class.java)

    fun typeOverrides(action: Action<TypeOverridesConfiguration>) {
        action.execute(typeOverrides)
    }

    @get:Nested
    @get:Optional
    val client: GenerateClientConfiguration = objects.newInstance(GenerateClientConfiguration::class.java)

    fun client(action: Action<GenerateClientConfiguration>) {
        action.execute(client)
    }

    @get:Nested
    @get:Optional
    val controller: GenerateControllerConfiguration =
        objects.newInstance(GenerateControllerConfiguration::class.java)

    fun controller(action: Action<GenerateControllerConfiguration>) {
        action.execute(controller)
    }

    @get:Nested
    @get:Optional
    val model: GenerateModelConfiguration =
        objects.newInstance(GenerateModelConfiguration::class.java)

    fun model(action: Action<GenerateModelConfiguration>) {
        action.execute(model)
    }

    @get:Input
    @get:Optional
    open val skip: Property<Boolean> = objects.property(Boolean::class.java)

}

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

    @get:Input
    @get:Optional
    val binary: Property<BinaryOverrideOption> = objects.property(BinaryOverrideOption::class.java)

    @get:Internal
    @Suppress("VariableNaming")
    val ByteArray: BinaryOverrideOption = BinaryOverrideOption.ByteArray

    @get:Internal
    @Suppress("VariableNaming")
    val InputStream: BinaryOverrideOption = BinaryOverrideOption.InputStream

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

    @get:Input
    @get:Optional
    val suffix: Property<CharSequence> = objects.property(CharSequence::class.java)

    @get:Input
    @get:Optional
    val serializationLibrary: Property<SerializationLibraryOption> =
        objects.property(SerializationLibraryOption::class.java)

    @get:Internal
    @Suppress("VariableNaming")
    val Jackson: SerializationLibraryOption = SerializationLibraryOption.Jackson

    @get:Internal
    @Suppress("VariableNaming")
    val Kotlin: SerializationLibraryOption = SerializationLibraryOption.Kotlin

}

abstract class GenerateTaskDefaults @Inject constructor(layouts: ProjectLayout) {

    // apiFile and basePackage are meant to be different for each
    // configuration and therefore cannot have a default value.

    val enabled: Boolean = true

    val disabled: Boolean = false

    abstract val apiFragments: ConfigurableFileCollection

    abstract val externalReferenceResolution: Property<ExternalReferencesResolutionOption>

    val targeted: ExternalReferencesResolutionOption = ExternalReferencesResolutionOption.targeted

    val aggressive: ExternalReferencesResolutionOption = ExternalReferencesResolutionOption.aggressive

    abstract val outputDirectory: DirectoryProperty

    abstract val sourcesPath: Property<CharSequence>

    abstract val resourcesPath: Property<CharSequence>

    abstract val validationLibrary: Property<ValidationLibraryOption>

    @Suppress("VariableNaming")
    val Jakarta: ValidationLibraryOption = ValidationLibraryOption.Jakarta

    @Suppress("VariableNaming")
    val Javax: ValidationLibraryOption = ValidationLibraryOption.Javax

    @Suppress("VariableNaming")
    val NoValidation: ValidationLibraryOption = ValidationLibraryOption.NoValidation

    abstract val quarkusReflectionConfig: Property<Boolean>

    @get:Nested
    abstract val typeOverrides: TypeOverridesDefaults

    fun typeOverrides(action: Action<TypeOverridesDefaults>) {
        action.execute(typeOverrides)
    }

    @get:Nested
    abstract val client: GenerateClientDefaults

    fun client(action: Action<GenerateClientDefaults>) {
        action.execute(client)
    }

    @get:Nested
    abstract val controller: GenerateControllerDefaults

    fun controller(action: Action<GenerateControllerDefaults>) {
        action.execute(controller)
    }

    @get:Nested
    abstract val model: GenerateModelDefaults

    fun model(action: Action<GenerateModelDefaults>) {
        action.execute(model)
    }

    abstract val skip: Property<Boolean>

    init {
        externalReferenceResolution.convention(ExternalReferencesResolutionOption.targeted)
        outputDirectory.convention(layouts.buildDirectory.dir("generated/sources/fabrikt"))
        sourcesPath.convention("src/main/kotlin")
        resourcesPath.convention("src/main/resources")
        validationLibrary.convention(ValidationLibraryOption.Jakarta)
        quarkusReflectionConfig.convention(false)
        skip.convention(false)
    }

}

abstract class TypeOverridesDefaults {

    abstract val datetime: Property<DateTimeOverrideOption>

    @Suppress("VariableNaming")
    val OffsetDateTime: DateTimeOverrideOption = DateTimeOverrideOption.OffsetDateTime

    @Suppress("VariableNaming")
    val Instant: DateTimeOverrideOption = DateTimeOverrideOption.Instant

    @Suppress("VariableNaming")
    val LocalDateTime: DateTimeOverrideOption = DateTimeOverrideOption.LocalDateTime

    abstract val binary: Property<BinaryOverrideOption>

    @Suppress("VariableNaming")
    val ByteArray: BinaryOverrideOption = BinaryOverrideOption.ByteArray

    @Suppress("VariableNaming")
    val InputStream: BinaryOverrideOption = BinaryOverrideOption.InputStream

    init {
        datetime.convention(DateTimeOverrideOption.OffsetDateTime)
        binary.convention(BinaryOverrideOption.ByteArray)
    }
}

abstract class GenerateClientDefaults {

    abstract val generate: Property<Boolean>

    abstract val resilience4j: Property<Boolean>

    abstract val suspendModifier: Property<Boolean>

    abstract val springResponseEntityWrapper: Property<Boolean>

    abstract val target: Property<ClientTargetOption>

    @Suppress("VariableNaming")
    val OkHttp: ClientTargetOption = ClientTargetOption.OkHttp

    @Suppress("VariableNaming")
    val OpenFeign: ClientTargetOption = ClientTargetOption.OpenFeign

    init {
        generate.convention(false)
        resilience4j.convention(false)
        suspendModifier.convention(false)
        springResponseEntityWrapper.convention(false)
        target.convention(ClientTargetOption.OkHttp)
    }

}

abstract class GenerateControllerDefaults {

    abstract val generate: Property<Boolean>

    abstract val authentication: Property<Boolean>

    abstract val suspendModifier: Property<Boolean>

    abstract val completionStage: Property<Boolean>

    abstract val target: Property<ControllerTargetOption>

    @Suppress("VariableNaming")
    val Spring: ControllerTargetOption = ControllerTargetOption.Spring

    @Suppress("VariableNaming")
    val Micronaut: ControllerTargetOption = ControllerTargetOption.Micronaut

    init {
        generate.convention(false)
        authentication.convention(false)
        suspendModifier.convention(false)
        completionStage.convention(false)
        target.convention(ControllerTargetOption.Spring)
    }

}

abstract class GenerateModelDefaults {

    abstract val generate: Property<Boolean>

    abstract val extensibleEnums: Property<Boolean>

    abstract val javaSerialization: Property<Boolean>

    abstract val quarkusReflection: Property<Boolean>

    abstract val micronautIntrospection: Property<Boolean>

    abstract val micronautReflection: Property<Boolean>

    abstract val includeCompanionObject: Property<Boolean>

    abstract val sealedInterfacesForOneOf: Property<Boolean>

    abstract val nonNullMapValues: Property<Boolean>

    abstract val ignoreUnknownProperties: Property<Boolean>

    abstract val suffix: Property<CharSequence>

    abstract val serializationLibrary: Property<SerializationLibraryOption>

    @Suppress("VariableNaming")
    val Jackson: SerializationLibraryOption = SerializationLibraryOption.Jackson

    @Suppress("VariableNaming")
    val Kotlin: SerializationLibraryOption = SerializationLibraryOption.Kotlin

    init {
        generate.convention(true)
        extensibleEnums.convention(false)
        javaSerialization.convention(false)
        quarkusReflection.convention(false)
        micronautIntrospection.convention(false)
        micronautReflection.convention(false)
        includeCompanionObject.convention(false)
        sealedInterfacesForOneOf.convention(false)
        nonNullMapValues.convention(false)
        ignoreUnknownProperties.convention(false)
        serializationLibrary.convention(SerializationLibraryOption.Jackson)
    }

}
