package ch.acanda.gradle.fabrikt

import ch.acanda.gradle.fabrikt.generator.generate
import com.cjbooms.fabrikt.cli.ClientCodeGenTargetType
import com.cjbooms.fabrikt.cli.CodeGenTypeOverride
import com.cjbooms.fabrikt.cli.ControllerCodeGenOptionType
import com.cjbooms.fabrikt.cli.ControllerCodeGenTargetType
import com.cjbooms.fabrikt.cli.ModelCodeGenOptionType
import com.cjbooms.fabrikt.cli.ValidationLibrary
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
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
import javax.inject.Inject

abstract class FabriktGenerateTask : DefaultTask() {

    @get:Nested
    abstract val configurations: ListProperty<GenerateTaskConfiguration>

    @TaskAction
    fun generate() {
        configurations.get().forEach { config ->
            logger.info("Generate ${config.apiFile.get()}")
            generate(config)
        }
    }

}

class GenerateTaskConfiguration @Inject constructor(project: Project) {

    @get:InputFile
    val apiFile: RegularFileProperty = project.objects.fileProperty()

    @get:InputFiles
    val apiFragments: ConfigurableFileCollection = project.objects.fileCollection()

    @get:Input
    val basePackage: Property<CharSequence> = project.objects.property(CharSequence::class.java)

    @get:OutputDirectory
    @get:Optional
    val outputDirectory: DirectoryProperty = project.objects.directoryProperty()
        .convention(project.layout.buildDirectory.dir("generated/fabrikt"))

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
    val typeOverrides: Property<CodeGenTypeOverride> = project.objects.property(CodeGenTypeOverride::class.java)

    @get:Input
    @get:Optional
    val validationLibrary: Property<ValidationLibrary> = project.objects.property(ValidationLibrary::class.java)
        .convention(ValidationLibrary.JAKARTA_VALIDATION)

    @get:Input
    @get:Optional
    val quarkusReflectionConfig: Property<Boolean> = project.objects.property(Boolean::class.java)
        .convention(false)

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

    override fun toString(): String {
        return """{ "client": $client }"""
    }

}

open class GenerateClientConfiguration @Inject constructor(objects: ObjectFactory) {

    @get:Input
    @get:Optional
    val enabled: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

    @get:Input
    @get:Optional
    val resilience4j: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

    @get:Input
    @get:Optional
    val suspendModifier: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

    @get:Input
    @get:Optional
    val target: Property<ClientCodeGenTargetType> = objects.property(ClientCodeGenTargetType::class.java)
        .convention(ClientCodeGenTargetType.OK_HTTP)

    override fun toString(): String {
        return """{ "suspendModifier": ${suspendModifier.orNull} }"""
    }
}

open class GenerateControllerConfiguration @Inject constructor(objects: ObjectFactory) {

    @get:Input
    @get:Optional
    val enabled: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

    @get:Input
    @get:Optional
    val options: SetProperty<ControllerCodeGenOptionType> = objects.setProperty(ControllerCodeGenOptionType::class.java)

    @get:Input
    @get:Optional
    val target: Property<ControllerCodeGenTargetType> = objects.property(ControllerCodeGenTargetType::class.java)
        .convention(ControllerCodeGenTargetType.SPRING)

}

open class GenerateModelConfiguration @Inject constructor(objects: ObjectFactory) {

    @get:Input
    @get:Optional
    val enabled: Property<Boolean> = objects.property(Boolean::class.java).convention(true)

    @get:Input
    @get:Optional
    val options: SetProperty<ModelCodeGenOptionType> = objects.setProperty(ModelCodeGenOptionType::class.java)

}
