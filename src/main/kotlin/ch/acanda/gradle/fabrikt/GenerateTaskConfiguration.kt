package ch.acanda.gradle.fabrikt

import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import javax.inject.Inject

abstract class GenerateTaskConfiguration @Inject constructor(
    private val name: String,
) : Named {

    @Internal
    override fun getName() = name

    @get:Internal
    val enabled: Boolean = true

    @get:Internal
    val disabled: Boolean = false

    @get:InputFile
    abstract val apiFile: RegularFileProperty

    @get:InputFiles
    @get:Optional
    abstract val apiFragments: ConfigurableFileCollection

    @get:Input
    @get:Optional
    abstract val externalReferenceResolution: Property<ExternalReferencesResolutionOption>

    @get:Internal
    val targeted: ExternalReferencesResolutionOption = ExternalReferencesResolutionOption.targeted

    @get:Internal
    val aggressive: ExternalReferencesResolutionOption = ExternalReferencesResolutionOption.aggressive

    @get:Input
    abstract val basePackage: Property<CharSequence>

    @get:OutputDirectory
    @get:Optional
    abstract val outputDirectory: DirectoryProperty

    @get:Input
    @get:Optional
    abstract val sourcesPath: Property<CharSequence>

    @get:Input
    @get:Optional
    abstract val resourcesPath: Property<CharSequence>

    @get:Input
    @get:Optional
    abstract val validationLibrary: Property<ValidationLibraryOption>

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
    abstract val quarkusReflectionConfig: Property<Boolean>

    @get:Nested
    @get:Optional
    abstract val typeOverrides: TypeOverridesConfiguration

    fun typeOverrides(action: Action<TypeOverridesConfiguration>) {
        action.execute(typeOverrides)
    }

    @get:Nested
    @get:Optional
    abstract val client: GenerateClientConfiguration

    fun client(action: Action<GenerateClientConfiguration>) {
        action.execute(client)
    }

    @get:Nested
    @get:Optional
    abstract val controller: GenerateControllerConfiguration

    fun controller(action: Action<GenerateControllerConfiguration>) {
        action.execute(controller)
    }

    @get:Nested
    @get:Optional
    abstract val model: GenerateModelConfiguration

    fun model(action: Action<GenerateModelConfiguration>) {
        action.execute(model)
    }

    @get:Input
    @get:Optional
    abstract val skip: Property<Boolean>

}

abstract class TypeOverridesConfiguration {

    @get:Input
    @get:Optional
    abstract val datetime: Property<DateTimeOverrideOption>

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
    abstract val binary: Property<BinaryOverrideOption>

    @get:Internal
    @Suppress("VariableNaming")
    val ByteArray: BinaryOverrideOption = BinaryOverrideOption.ByteArray

    @get:Internal
    @Suppress("VariableNaming")
    val InputStream: BinaryOverrideOption = BinaryOverrideOption.InputStream

}

abstract class GenerateClientConfiguration {

    @get:Input
    @get:Optional
    abstract val generate: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val resilience4j: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val suspendModifier: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val springResponseEntityWrapper: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val target: Property<ClientTargetOption>

    @get:Internal
    @Suppress("VariableNaming")
    val OkHttp: ClientTargetOption = ClientTargetOption.OkHttp

    @get:Internal
    @Suppress("VariableNaming")
    val OpenFeign: ClientTargetOption = ClientTargetOption.OpenFeign

}

abstract class GenerateControllerConfiguration {

    @get:Input
    @get:Optional
    abstract val generate: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val authentication: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val suspendModifier: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val completionStage: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val target: Property<ControllerTargetOption>

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

abstract class GenerateModelConfiguration {

    @get:Input
    @get:Optional
    abstract val generate: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val extensibleEnums: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val javaSerialization: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val quarkusReflection: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val micronautIntrospection: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val micronautReflection: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val includeCompanionObject: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val sealedInterfacesForOneOf: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val nonNullMapValues: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val ignoreUnknownProperties: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val suffix: Property<CharSequence>

    @get:Input
    @get:Optional
    abstract val serializationLibrary: Property<SerializationLibraryOption>

    @get:Internal
    @Suppress("VariableNaming")
    val Jackson: SerializationLibraryOption = SerializationLibraryOption.Jackson

    @get:Internal
    @Suppress("VariableNaming")
    val Kotlin: SerializationLibraryOption = SerializationLibraryOption.Kotlin

}
