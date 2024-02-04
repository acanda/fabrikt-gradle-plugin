@file:Generated("ch.acanda.gradle.fabrikt.build.ExtensionGenerator")

package ch.acanda.gradle.fabrikt

import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested
import javax.annotation.processing.Generated
import javax.inject.Inject
import kotlin.Boolean

open class FabriktExtension @Inject constructor(objects: ObjectFactory) :
    NamedDomainObjectContainer<FabriktGenerateExtension> by
    objects.domainObjectContainer(FabriktGenerateExtension::class.java) {

    fun generate(name: String, action: Action<FabriktGenerateExtension>) {
        register(name, action)
    }

}

open class FabriktGenerateExtension @Inject constructor(private val name: String, objects: ObjectFactory) : Named {

    val enabled: Boolean = true

    val disabled: Boolean = false

    val apiFile: RegularFileProperty = objects.fileProperty()

    val apiFragments: ConfigurableFileCollection = objects.fileCollection()

    val externalReferenceResolution: Property<ExternalReferencesResolutionOption> =
        objects.property(ExternalReferencesResolutionOption::class.java)

    val targeted: ExternalReferencesResolutionOption =
        ExternalReferencesResolutionOption.targeted

    val aggressive: ExternalReferencesResolutionOption =
        ExternalReferencesResolutionOption.aggressive

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

    val quarkusReflectionConfig: Property<Boolean> = objects.property(Boolean::class.java)

    @Nested
    val typeOverrides: TypeOverridesExtension =
        objects.newInstance(TypeOverridesExtension::class.java)

    @Nested
    val client: ClientExtension = objects.newInstance(ClientExtension::class.java)

    @Nested
    val controller: ControllerExtension = objects.newInstance(ControllerExtension::class.java)

    @Nested
    val model: ModelExtension = objects.newInstance(ModelExtension::class.java)

    override fun getName(): String = name

    fun typeOverrides(action: Action<TypeOverridesExtension>) {
        action.execute(typeOverrides)
    }

    fun client(action: Action<ClientExtension>) {
        action.execute(client)
    }

    fun controller(action: Action<ControllerExtension>) {
        action.execute(controller)
    }

    fun model(action: Action<ModelExtension>) {
        action.execute(model)
    }

}

@Suppress("VariableNaming")
open class TypeOverridesExtension @Inject constructor(objects: ObjectFactory) {

    val datetime: Property<DateTimeOverrideOption> =
        objects.property(DateTimeOverrideOption::class.java)

    val OffsetDateTime: DateTimeOverrideOption = DateTimeOverrideOption.OffsetDateTime

    val Instant: DateTimeOverrideOption = DateTimeOverrideOption.Instant

    val LocalDateTime: DateTimeOverrideOption = DateTimeOverrideOption.LocalDateTime

}

open class ClientExtension @Inject constructor(objects: ObjectFactory) {

    val generate: Property<Boolean> = objects.property(Boolean::class.java)

    val resilience4j: Property<Boolean> = objects.property(Boolean::class.java)

    val suspendModifier: Property<Boolean> = objects.property(Boolean::class.java)

    val target: Property<ClientTargetOption> = objects.property(ClientTargetOption::class.java)

    @Suppress("VariableNaming")
    val OkHttp: ClientTargetOption = ClientTargetOption.OkHttp

    @Suppress("VariableNaming")
    val OpenFeign: ClientTargetOption = ClientTargetOption.OpenFeign

}

open class ControllerExtension @Inject constructor(objects: ObjectFactory) {

    val generate: Property<Boolean> = objects.property(Boolean::class.java)

    val authentication: Property<Boolean> = objects.property(Boolean::class.java)

    val suspendModifier: Property<Boolean> = objects.property(Boolean::class.java)

    val target: Property<ControllerTargetOption> =
        objects.property(ControllerTargetOption::class.java)

    @Suppress("VariableNaming")
    val Spring: ControllerTargetOption = ControllerTargetOption.Spring

    @Suppress("VariableNaming")
    val Micronaut: ControllerTargetOption = ControllerTargetOption.Micronaut

}

open class ModelExtension @Inject constructor(objects: ObjectFactory) {

    val generate: Property<Boolean> = objects.property(Boolean::class.java)

    val extensibleEnums: Property<Boolean> = objects.property(Boolean::class.java)

    val javaSerialization: Property<Boolean> = objects.property(Boolean::class.java)

    val quarkusReflection: Property<Boolean> = objects.property(Boolean::class.java)

    val micronautIntrospection: Property<Boolean> = objects.property(Boolean::class.java)

    val micronautReflection: Property<Boolean> = objects.property(Boolean::class.java)

    val includeCompanionObject: Property<Boolean> = objects.property(Boolean::class.java)

    val sealedInterfacesForOneOf: Property<Boolean> = objects.property(Boolean::class.java)

    val ignoreUnknownProperties: Property<Boolean> = objects.property(Boolean::class.java)

}
