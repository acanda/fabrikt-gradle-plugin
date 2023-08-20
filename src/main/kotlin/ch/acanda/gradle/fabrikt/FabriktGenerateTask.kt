package ch.acanda.gradle.fabrikt

import ch.acanda.gradle.fabrikt.generator.generate
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

abstract class FabriktGenerateTask @Inject constructor(objects: ObjectFactory) : DefaultTask() {

    @get:InputFile
    abstract val apiFile: RegularFileProperty

    @get:Input
    abstract val basePackage: Property<String>

    @get:OutputDirectory
    @get:Optional
    val outputDirectory: DirectoryProperty = objects.directoryProperty()
        .convention(project.layout.buildDirectory.dir("generated/fabrikt"))

    @TaskAction
    fun generate() {
        logger.info("Generate ${apiFile.get()}")
        generate(apiFile.get().asFile.toPath(), basePackage.get(), outputDirectory.get().asFile.toPath())
    }

}
