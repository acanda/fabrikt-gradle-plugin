package ch.acanda.gradle.fabrikt

import ch.acanda.gradle.fabrikt.generator.generate
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

abstract class FabriktGenerateTask : DefaultTask() {

    @get:InputFile
    abstract val apiFile: RegularFileProperty

    @get:Input
    abstract val basePackage: Property<String>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun generate() {
        logger.info("Generate ${apiFile.get()}")
        generate(apiFile.get().asFile.toPath(), basePackage.get(), outputDirectory.get().asFile.toPath())
    }

}
