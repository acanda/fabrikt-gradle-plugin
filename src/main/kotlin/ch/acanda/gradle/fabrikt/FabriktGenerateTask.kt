package ch.acanda.gradle.fabrikt

import ch.acanda.gradle.fabrikt.generator.generate
import com.cjbooms.fabrikt.cli.CodeGenTypeOverride
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.logging.progress.ProgressLogger
import org.gradle.internal.logging.progress.ProgressLoggerFactory
import javax.inject.Inject

abstract class FabriktGenerateTask : DefaultTask() {

    @get:Nested
    abstract val configurations: ListProperty<GenerateTaskConfiguration>

    @get:Inject
    abstract val progressLoggerFactory: ProgressLoggerFactory

    @TaskAction
    fun generate() {
        val configs = configurations.get()
        Progress(progressLoggerFactory, configs.size).use { progress ->
            configs.forEach { config ->
                progress.log(config.apiFile.get())
                generate(config)
            }
        }
    }

}

private class Progress(factory: ProgressLoggerFactory, val total: Int) : AutoCloseable {

    private val progressLogger: ProgressLogger = factory.newOperation(FabriktGenerateTask::class.java)
    private var count = 0

    init {
        progressLogger.start("Generating Kotlin code with Fabrikt", "[0/$total]")
    }

    fun log(apiFile: RegularFile) {
        count++
        progressLogger.progress("[$count/$total] generating code for $apiFile...")
    }

    override fun close() {
        progressLogger.completed()
    }

}

class GenerateTaskConfiguration @Inject constructor(project: Project) {

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

    @get:Input
    @get:Optional
    val quarkusReflectionConfig: Property<Boolean> = project.objects.property(Boolean::class.java)
        .convention(false)

    @get:Nested
    @get:Optional
    val typeOverrides: TypeOverridesConfiguration = project.objects.newInstance(TypeOverridesConfiguration::class.java)

    @get:Nested
    @get:Optional
    val client: GenerateClientConfiguration = project.objects.newInstance(GenerateClientConfiguration::class.java)

    @get:Nested
    @get:Optional
    val controller: GenerateControllerConfiguration =
        project.objects.newInstance(GenerateControllerConfiguration::class.java)

    @get:Nested
    @get:Optional
    val model: GenerateModelConfiguration =
        project.objects.newInstance(GenerateModelConfiguration::class.java)

    @get:Input
    @get:Optional
    val options: SetProperty<CodeGenTypeOverride> = project.objects.setProperty(CodeGenTypeOverride::class.java)

}

open class TypeOverridesConfiguration @Inject constructor(objects: ObjectFactory) {

    @get:Input
    @get:Optional
    val datetime: Property<DateTimeOverrideOption> = objects.property(DateTimeOverrideOption::class.java)

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
