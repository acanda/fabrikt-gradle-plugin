@file:Generated("ch.acanda.gradle.fabrikt.build.ExtensionGenerator")

package ch.acanda.gradle.fabrikt

import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
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

open class GenerateTaskExtension @Inject constructor(private val name: String, objects: ObjectFactory) : Named {

    override fun getName() = name

    val enabled: Boolean = true

    val disabled: Boolean = false

    val apiFile: RegularFileProperty = objects.fileProperty()

    val apiFragments: ConfigurableFileCollection = objects.fileCollection()

    val externalReferenceResolution: Property<ExternalReferencesResolutionOption> =
        objects.property(ExternalReferencesResolutionOption::class.java)

    val targeted: ExternalReferencesResolutionOption = ExternalReferencesResolutionOption.targeted

    val aggressive: ExternalReferencesResolutionOption = ExternalReferencesResolutionOption.aggressive

    val basePackage: Property<CharSequence> = objects.property(CharSequence::class.java)

    val outputDirectory: DirectoryProperty = objects.directoryProperty()

    val sourcesPath: Property<CharSequence> = objects.property(CharSequence::class.java)

    val resourcesPath: Property<CharSequence> = objects.property(CharSequence::class.java)

    val validationLibrary: Property<ValidationLibraryOption> =
        objects.property(ValidationLibraryOption::class.java)

    @Suppress("VariableNaming")
    val Jakarta: ValidationLibraryOption = ValidationLibraryOption.Jakarta

    @Suppress("VariableNaming")
    val Javax: ValidationLibraryOption = ValidationLibraryOption.Javax

    @Suppress("VariableNaming")
    val NoValidation: ValidationLibraryOption = ValidationLibraryOption.NoValidation

    val quarkusReflectionConfig: Property<Boolean> = objects.property(Boolean::class.java)

    val typeOverrides: TypeOverridesExtension = objects.newInstance(TypeOverridesExtension::class.java)

    fun typeOverrides(action: Action<TypeOverridesExtension>) {
        action.execute(typeOverrides)
    }

    val client: GenerateClientExtension = objects.newInstance(GenerateClientExtension::class.java)

    fun client(action: Action<GenerateClientExtension>) {
        action.execute(client)
    }

    val controller: GenerateControllerExtension =
        objects.newInstance(GenerateControllerExtension::class.java)

    fun controller(action: Action<GenerateControllerExtension>) {
        action.execute(controller)
    }

    val model: GenerateModelExtension =
        objects.newInstance(GenerateModelExtension::class.java)

    fun model(action: Action<GenerateModelExtension>) {
        action.execute(model)
    }

    open val skip: Property<Boolean> = objects.property(Boolean::class.java)

}

open class TypeOverridesExtension @Inject constructor(objects: ObjectFactory) {

    val datetime: Property<DateTimeOverrideOption> = objects.property(DateTimeOverrideOption::class.java)

    @Suppress("VariableNaming")
    val OffsetDateTime: DateTimeOverrideOption = DateTimeOverrideOption.OffsetDateTime

    @Suppress("VariableNaming")
    val Instant: DateTimeOverrideOption = DateTimeOverrideOption.Instant

    @Suppress("VariableNaming")
    val LocalDateTime: DateTimeOverrideOption = DateTimeOverrideOption.LocalDateTime

    val binary: Property<BinaryOverrideOption> = objects.property(BinaryOverrideOption::class.java)

    @Suppress("VariableNaming")
    val ByteArray: BinaryOverrideOption = BinaryOverrideOption.ByteArray

    @Suppress("VariableNaming")
    val InputStream: BinaryOverrideOption = BinaryOverrideOption.InputStream

}

open class GenerateClientExtension @Inject constructor(objects: ObjectFactory) {

    val generate: Property<Boolean> = objects.property(Boolean::class.java)

    val resilience4j: Property<Boolean> = objects.property(Boolean::class.java)

    val suspendModifier: Property<Boolean> = objects.property(Boolean::class.java)

    val springResponseEntityWrapper: Property<Boolean> = objects.property(Boolean::class.java)

    val target: Property<ClientTargetOption> = objects.property(ClientTargetOption::class.java)

    @Suppress("VariableNaming")
    val OkHttp: ClientTargetOption = ClientTargetOption.OkHttp

    @Suppress("VariableNaming")
    val OpenFeign: ClientTargetOption = ClientTargetOption.OpenFeign

}

open class GenerateControllerExtension @Inject constructor(objects: ObjectFactory) {

    val generate: Property<Boolean> = objects.property(Boolean::class.java)

    val authentication: Property<Boolean> = objects.property(Boolean::class.java)

    val suspendModifier: Property<Boolean> = objects.property(Boolean::class.java)

    val completionStage: Property<Boolean> = objects.property(Boolean::class.java)

    val target: Property<ControllerTargetOption> = objects.property(ControllerTargetOption::class.java)

    @Suppress("VariableNaming")
    val Spring: ControllerTargetOption = ControllerTargetOption.Spring

    @Suppress("VariableNaming")
    val Micronaut: ControllerTargetOption = ControllerTargetOption.Micronaut

    @Suppress("VariableNaming")
    val Ktor: ControllerTargetOption = ControllerTargetOption.Ktor

}

open class GenerateModelExtension @Inject constructor(objects: ObjectFactory) {

    val generate: Property<Boolean> = objects.property(Boolean::class.java)

    val extensibleEnums: Property<Boolean> = objects.property(Boolean::class.java)

    val javaSerialization: Property<Boolean> = objects.property(Boolean::class.java)

    val quarkusReflection: Property<Boolean> = objects.property(Boolean::class.java)

    val micronautIntrospection: Property<Boolean> = objects.property(Boolean::class.java)

    val micronautReflection: Property<Boolean> = objects.property(Boolean::class.java)

    val includeCompanionObject: Property<Boolean> = objects.property(Boolean::class.java)

    val sealedInterfacesForOneOf: Property<Boolean> = objects.property(Boolean::class.java)

    val nonNullMapValues: Property<Boolean> = objects.property(Boolean::class.java)

    val ignoreUnknownProperties: Property<Boolean> = objects.property(Boolean::class.java)

    val suffix: Property<CharSequence> = objects.property(CharSequence::class.java)

    val serializationLibrary: Property<SerializationLibraryOption> =
        objects.property(SerializationLibraryOption::class.java)

    @Suppress("VariableNaming")
    val Jackson: SerializationLibraryOption = SerializationLibraryOption.Jackson

    @Suppress("VariableNaming")
    val Kotlin: SerializationLibraryOption = SerializationLibraryOption.Kotlin

}
