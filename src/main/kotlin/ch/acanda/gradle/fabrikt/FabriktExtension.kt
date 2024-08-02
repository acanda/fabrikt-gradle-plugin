@file:Generated("ch.acanda.gradle.fabrikt.build.ExtensionGenerator")

package ch.acanda.gradle.fabrikt

import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Nested
import javax.annotation.processing.Generated
import javax.inject.Inject

open class FabriktExtension @Inject constructor(private val project: Project) :
    NamedDomainObjectContainer<GenerateTaskExtension> by
    project.objects.domainObjectContainer(GenerateTaskExtension::class.java) {

    private var defaultsAction: Action<GenerateTaskDefaults>? = null

    fun defaults(action: Action<GenerateTaskDefaults>) {
        defaultsAction = action
    }

    fun generate(name: String, action: Action<GenerateTaskExtension>) {
        register(name, action)
    }

    internal fun getDefaults(): Provider<GenerateTaskDefaults> =
        project.provider {
            project.objects.newInstance(GenerateTaskDefaults::class.java).also { defaultsAction?.execute(it) }
        }

    internal fun getGenerateExtensions(): Provider<out List<GenerateTaskExtension>> =
        project.provider { this.toList() }

    internal fun getGenerateExtension(name: String): Provider<GenerateTaskExtension> =
        project.provider { getByName(name) }

}

abstract class GenerateTaskExtension @Inject constructor() : Named {

    val enabled: Boolean = true

    val disabled: Boolean = false

    abstract val apiFile: RegularFileProperty

    abstract val apiFragments: ConfigurableFileCollection

    abstract val externalReferenceResolution: Property<ExternalReferencesResolutionOption>

    val targeted: ExternalReferencesResolutionOption = ExternalReferencesResolutionOption.targeted

    val aggressive: ExternalReferencesResolutionOption = ExternalReferencesResolutionOption.aggressive

    abstract val basePackage: Property<CharSequence>

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
    abstract val typeOverrides: TypeOverridesExtension

    fun typeOverrides(action: Action<TypeOverridesExtension>) {
        action.execute(typeOverrides)
    }

    @get:Nested
    abstract val client: GenerateClientExtension

    fun client(action: Action<GenerateClientExtension>) {
        action.execute(client)
    }

    @get:Nested
    abstract val controller: GenerateControllerExtension

    fun controller(action: Action<GenerateControllerExtension>) {
        action.execute(controller)
    }

    @get:Nested
    abstract val model: GenerateModelExtension

    fun model(action: Action<GenerateModelExtension>) {
        action.execute(model)
    }

    abstract val skip: Property<Boolean>

}

abstract class TypeOverridesExtension {

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

}

abstract class GenerateClientExtension {

    abstract val generate: Property<Boolean>

    abstract val resilience4j: Property<Boolean>

    abstract val suspendModifier: Property<Boolean>

    abstract val springResponseEntityWrapper: Property<Boolean>

    abstract val target: Property<ClientTargetOption>

    @Suppress("VariableNaming")
    val OkHttp: ClientTargetOption = ClientTargetOption.OkHttp

    @Suppress("VariableNaming")
    val OpenFeign: ClientTargetOption = ClientTargetOption.OpenFeign

}

abstract class GenerateControllerExtension {

    abstract val generate: Property<Boolean>

    abstract val authentication: Property<Boolean>

    abstract val suspendModifier: Property<Boolean>

    abstract val completionStage: Property<Boolean>

    abstract val target: Property<ControllerTargetOption>

    @Suppress("VariableNaming")
    val Spring: ControllerTargetOption = ControllerTargetOption.Spring

    @Suppress("VariableNaming")
    val Micronaut: ControllerTargetOption = ControllerTargetOption.Micronaut

    @Suppress("VariableNaming")
    val Ktor: ControllerTargetOption = ControllerTargetOption.Ktor

}

@Suppress("UnnecessaryAbstractClass")
abstract class GenerateModelExtension {

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

}
