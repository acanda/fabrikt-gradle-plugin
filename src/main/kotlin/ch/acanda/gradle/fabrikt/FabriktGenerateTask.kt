package ch.acanda.gradle.fabrikt

import ch.acanda.gradle.fabrikt.generator.generate
import com.cjbooms.fabrikt.cli.ClientCodeGenOptionType
import com.cjbooms.fabrikt.cli.ClientCodeGenTargetType
import com.cjbooms.fabrikt.cli.CodeGenerationType
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
    val targets: SetProperty<CodeGenerationType> = project.objects.setProperty(CodeGenerationType::class.java)
        .convention(setOf(CodeGenerationType.HTTP_MODELS))

    @get:Nested
    @get:Optional
    val client: GenerateClientConfiguration = project.objects.newInstance(GenerateClientConfiguration::class.java)

}

open class GenerateClientConfiguration @Inject constructor(objects: ObjectFactory) {

    @get:Input
    @get:Optional
    val enabled: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

    @get:Input
    @get:Optional
    val options: SetProperty<ClientCodeGenOptionType> = objects.setProperty(ClientCodeGenOptionType::class.java)

    @get:Input
    @get:Optional
    val target: Property<ClientCodeGenTargetType> = objects.property(ClientCodeGenTargetType::class.java)

}
