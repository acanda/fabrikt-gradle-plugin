package ch.acanda.gradle.fabrikt

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction

abstract class FabriktGenerateTask : DefaultTask() {

    @get:InputFile
    abstract val apiFile: RegularFileProperty

    @TaskAction
    fun generate() {
        logger.info("Generate ${apiFile.get()}")
    }

}
