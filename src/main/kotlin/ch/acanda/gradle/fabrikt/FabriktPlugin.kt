package ch.acanda.gradle.fabrikt

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property

class FabriktPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create("fabrikt", FabriktExtension::class.java)
        project.tasks.register("fabriktGenerate", FabriktGenerateTask::class.java) { task ->
            val configurations = extension.map { generate ->
                GenerateTaskConfiguration(project).apply {
                    apiFile.set(generate.apiFile)
                    basePackage.set(generate.basePackage)
                    outputDirectory.setIfPresent(generate.outputDirectory)
                }
            }
            task.configurations.set(configurations)
        }
    }

    private fun <T, P : Property<T>> P.setIfPresent(value: P) {
        if (value.isPresent) set(value)
    }

}
