package ch.acanda.gradle.fabrikt

import ch.acanda.gradle.fabrikt.generator.generate
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Named
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.problems.ProblemSpec
import org.gradle.api.problems.Problems
import org.gradle.api.problems.Severity
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.logging.progress.ProgressLogger
import org.gradle.internal.logging.progress.ProgressLoggerFactory
import javax.inject.Inject

abstract class FabriktGenerateTask @Inject constructor(
    private val progressLoggerFactory: ProgressLoggerFactory,
    problems: Problems
) : DefaultTask() {

    private val problemReporter = problems.forNamespace(FabriktPlugin.PLUGIN_ID)

    @get:Nested
    abstract val configurations: ListProperty<GenerateTaskConfiguration>

    @TaskAction
    fun generate() {
        val configs = configurations.get()
        Progress(progressLoggerFactory, configs.size).use { progress ->
            configs.forEach { config ->
                val apiFile = config.apiFile.get()
                val skip = config.skip.get()
                progress.log(apiFile, skip)
                try {
                    generate(config)
                } catch (e: GeneratorException) {
                    progress.fail(apiFile)
                    problemReporter.rethrowing(e, generatorProblem(e, config.name))
                }
            }
        }
    }

    private fun generatorProblem(e: GeneratorException, name: String) = Action { problem: ProblemSpec ->
        problem.category("code-generation", "openapi", name.lowercase())
            .label("Fabrikt failed to generate code for configuration $name.")
            .details("Fabrikt failed to generate code for the OpenAPI specification ${e.apiFile}.")
            .severity(Severity.ERROR)
    }

}

private class Progress(factory: ProgressLoggerFactory, val total: Int) : AutoCloseable {

    private val progressLogger: ProgressLogger = factory.newOperation(FabriktGenerateTask::class.java)
    private var count = 0
    private var failed = false

    init {
        progressLogger.start("Generating Kotlin code with Fabrikt", "[0/$total]")
    }

    fun log(apiFile: RegularFile, skip: Boolean) {
        count++
        val skipMsg = if (skip) " skip" else " "
        progressLogger.progress("[$count/$total]$skipMsg generating code for $apiFile...")
    }

    fun fail(apiFile: RegularFile) {
        failed = true
        progressLogger.progress("[$count/$total] generating code for $apiFile...", true)
    }

    override fun close() {
        progressLogger.completed(null, failed)
    }

}

open class GenerateTaskConfiguration @Inject constructor(private val name: String, project: Project) : Named {

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
            .convention(ExternalReferencesResolutionOption.targeted)

    @get:Internal
    val targeted: ExternalReferencesResolutionOption = ExternalReferencesResolutionOption.targeted

    @get:Internal
    val aggressive: ExternalReferencesResolutionOption = ExternalReferencesResolutionOption.aggressive

    @get:Input
    val basePackage: Property<CharSequence> = project.objects.property(CharSequence::class.java)

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

    @get:Input
    @get:Optional
    val quarkusReflectionConfig: Property<Boolean> = project.objects.property(Boolean::class.java)
        .convention(false)

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
    val skip: Property<Boolean> = project.objects.property(Boolean::class.java)
        .convention(false)

}

open class TypeOverridesConfiguration @Inject constructor(objects: ObjectFactory) {

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

open class GenerateClientConfiguration @Inject constructor(objects: ObjectFactory) {

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
    val target: Property<ClientTargetOption> = objects.property(ClientTargetOption::class.java)
        .convention(ClientTargetOption.OkHttp)

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
    val generate: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

    @get:Input
    @get:Optional
    val authentication: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

    @get:Input
    @get:Optional
    val suspendModifier: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

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

open class GenerateModelConfiguration @Inject constructor(objects: ObjectFactory) {

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
    val ignoreUnknownProperties: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

}
