package ch.acanda.gradle.fabrikt

import org.gradle.api.Plugin
import org.gradle.api.Project

class FabriktPlugin :  Plugin<Project> {

    override fun apply(project: Project) {
        project.tasks.register("fabriktGenerate", FabriktGenerateTask::class.java)
    }

}
