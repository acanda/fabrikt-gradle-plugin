package ch.acanda.gradle.fabrikt

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property

class FabriktPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val ext = project.extensions.create("fabrikt", FabriktExtension::class.java)
        project.tasks.register("fabriktGenerate", FabriktGenerateTask::class.java) { task ->
            task.apiFile.set(ext.apiFile)
            task.basePackage.set(ext.basePackage)
            task.outputDirectory.setIfPresent(ext.outputDirectory)
        }
    }

    private fun <T, P : Property<T>> P.setIfPresent(value: P) {
        if (value.isPresent) set(value)
    }

}
