package ch.acanda.gradle.fabrikt

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.HasMultipleValues
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider

class FabriktPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create("fabrikt", FabriktExtension::class.java)
        project.tasks.register("fabriktGenerate", FabriktGenerateTask::class.java) { task ->
            val configurations = extension.map { generate ->
                GenerateTaskConfiguration(project).apply {
                    apiFile.set(generate.apiFile)
                    apiFragments.setFrom(generate.apiFragments)
                    basePackage.set(generate.basePackage)
                    outputDirectory.setIfPresent(generate.outputDirectory)
                    with(client) {
                        enabled.setIfPresent(generate.client.enabled)
                        options.setIfPresent(generate.client.options)
                        target.setIfPresent(generate.client.target)
                    }
                    with(controller) {
                        enabled.setIfPresent(generate.controller.enabled)
                        options.setIfPresent(generate.controller.options)
                        target.setIfPresent(generate.controller.target)
                    }
                    with(model) {
                        enabled.setIfPresent(generate.model.enabled)
                        options.setIfPresent(generate.model.options)
                    }
                }
            }
            task.configurations.set(configurations)
        }
    }

    private fun <T, P : Property<T>> P.setIfPresent(value: P) {
        if (value.isPresent) set(value)
    }

    private fun <T, M, P> M.setIfPresent(value: P)
        where M : HasMultipleValues<T>, P : Provider<out Iterable<T>> {
        if (value.isPresent) set(value)
    }

}
