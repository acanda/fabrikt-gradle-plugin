package ch.acanda.gradle.fabrikt

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class FabriktGenerateTask : DefaultTask() {

    @TaskAction
    fun generate() {
        logger.info("Generate")
    }

}
