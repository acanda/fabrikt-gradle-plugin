package ch.acanda.gradle.fabrikt

import ch.acanda.gradle.fabrikt.generator.generate
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
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
                generate(apiFile.get().asFile.toPath(), basePackage.get(), outputDirectory.get().asFile.toPath())
            }
        }
    }

}

class GenerateTaskConfiguration @Inject constructor(project: Project) {

    @get:InputFile
    val apiFile: RegularFileProperty = project.objects.fileProperty()

    @get:Input
    val basePackage: Property<String> = project.objects.property(String::class.java)

    @get:OutputDirectory
    @get:Optional
    val outputDirectory: DirectoryProperty = project.objects.directoryProperty()
        .convention(project.layout.buildDirectory.dir("generated/fabrikt"))

}
