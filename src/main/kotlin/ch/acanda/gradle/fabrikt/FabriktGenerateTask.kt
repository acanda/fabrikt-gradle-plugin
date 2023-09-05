package ch.acanda.gradle.fabrikt

import ch.acanda.gradle.fabrikt.generator.generate
import com.cjbooms.fabrikt.cli.CodeGenerationType
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
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
            with(config) {
                logger.info("Generate ${apiFile.get()}")
                generate(
                    apiFile.get().asFile.toPath(),
                    apiFragments.files.mapTo(mutableSetOf()) { it.toPath() },
                    basePackage.get(),
                    outputDirectory.get().asFile.toPath(),
                    targets.get()
                )
            }
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

}
