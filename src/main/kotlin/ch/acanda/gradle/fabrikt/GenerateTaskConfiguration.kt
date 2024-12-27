package ch.acanda.gradle.fabrikt

import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
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
