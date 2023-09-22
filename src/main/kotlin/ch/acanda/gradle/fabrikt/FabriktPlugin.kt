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
                    apiFragments.setFrom(generate.apiFragments)
                    basePackage.set(generate.basePackage)
                    outputDirectory.setIfPresent(generate.outputDirectory)
                    sourcesPath.setIfPresent(generate.sourcesPath)
                    resourcesPath.setIfPresent(generate.resourcesPath)
                    typeOverrides.setIfPresent(generate.typeOverrides)
                    validationLibrary.setIfPresent(generate.validationLibrary)
                    quarkusReflectionConfig.setIfPresent(generate.quarkusReflectionConfig)
                    with(client) {
                        enabled.setIfPresent(generate.client.enabled)
                        resilience4j.setIfPresent(generate.client.resilience4j)
                        suspendModifier.setIfPresent(generate.client.suspendModifier)
                        target.setIfPresent(generate.client.target)
                    }
                    with(controller) {
                        enabled.setIfPresent(generate.controller.enabled)
                        authentication.setIfPresent(generate.controller.authentication)
                        suspendModifier.setIfPresent(generate.controller.suspendModifier)
                        target.setIfPresent(generate.controller.target)
                    }
                    with(model) {
                        enabled.setIfPresent(generate.model.enabled)
                        extensibleEnums.setIfPresent(generate.model.extensibleEnums)
                        javaSerialization.setIfPresent(generate.model.javaSerialization)
                        quarkusReflection.setIfPresent(generate.model.quarkusReflection)
                        micronautIntrospection.setIfPresent(generate.model.micronautIntrospection)
                        micronautReflection.setIfPresent(generate.model.micronautReflection)
                        includeCompanionObject.setIfPresent(generate.model.includeCompanionObject)
                        sealedInterfacesForOneOf.setIfPresent(generate.model.sealedInterfacesForOneOf)
                    }
                }
            }
            task.configurations.set(configurations)
        }
    }

    private fun <T, P : Property<T>> P.setIfPresent(value: P) {
        if (value.isPresent) set(value)
    }

}
