package ch.acanda.gradle.fabrikt

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.provider.Property
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.plugins.ide.idea.model.IdeaModel

class FabriktPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create("fabrikt", FabriktExtension::class.java)

        val fabriktGenerateTask = project.tasks.register("fabriktGenerate", FabriktGenerateTask::class.java) { task ->
            val configurations = extension.map { project.createGenerateTaskConfiguration(it) }
            task.configurations.set(configurations)
            project.addOutputDirectoryToKotlinSourceSet(configurations)
        }

        project.addCompileKotlinDependsOn(fabriktGenerateTask)
        project.addGeneratedDirectoriesToIdea(extension)
    }

    private fun Project.createGenerateTaskConfiguration(ext: FabriktGenerateExtension) =
        GenerateTaskConfiguration(project).apply {
            apiFile.set(ext.apiFile)
            apiFragments.setFrom(ext.apiFragments)
            basePackage.set(ext.basePackage)
            outputDirectory.setIfPresent(ext.outputDirectory)
            sourcesPath.setIfPresent(ext.sourcesPath)
            resourcesPath.setIfPresent(ext.resourcesPath)
            typeOverrides.setIfPresent(ext.typeOverrides)
            validationLibrary.setIfPresent(ext.validationLibrary)
            quarkusReflectionConfig.setIfPresent(ext.quarkusReflectionConfig)
            with(client) {
                enabled.setIfPresent(ext.client.enabled)
                resilience4j.setIfPresent(ext.client.resilience4j)
                suspendModifier.setIfPresent(ext.client.suspendModifier)
                target.setIfPresent(ext.client.target)
            }
            with(controller) {
                enabled.setIfPresent(ext.controller.enabled)
                authentication.setIfPresent(ext.controller.authentication)
                suspendModifier.setIfPresent(ext.controller.suspendModifier)
                target.setIfPresent(ext.controller.target)
            }
            with(model) {
                enabled.setIfPresent(ext.model.enabled)
                extensibleEnums.setIfPresent(ext.model.extensibleEnums)
                javaSerialization.setIfPresent(ext.model.javaSerialization)
                quarkusReflection.setIfPresent(ext.model.quarkusReflection)
                micronautIntrospection.setIfPresent(ext.model.micronautIntrospection)
                micronautReflection.setIfPresent(ext.model.micronautReflection)
                includeCompanionObject.setIfPresent(ext.model.includeCompanionObject)
                sealedInterfacesForOneOf.setIfPresent(ext.model.sealedInterfacesForOneOf)
                ignoreUnknownProperties.setIfPresent(ext.model.ignoreUnknownProperties)
            }
        }

    private fun Project.addOutputDirectoryToKotlinSourceSet(configurations: List<GenerateTaskConfiguration>) {
        val sourceSets = project.extensions.findByName("sourceSets") as SourceSetContainer?
        val srcDirSet = sourceSets
            ?.findByName("main")
            ?.extensions
            ?.findByName("kotlin") as SourceDirectorySet?

        if (srcDirSet == null) {
            val msg = "Unable to find the source set \"main/kotlin\" and add the Fabrikt output directories."
            project.logger.info(msg)
        } else {
            val srcDirs = configurations.map { config ->
                config.outputDirectory.flatMap { it.dir(config.sourcesPath) }
            }
            srcDirSet.srcDirs(srcDirs)
        }
    }

    private fun Project.addCompileKotlinDependsOn(task: TaskProvider<FabriktGenerateTask>) {
        val compileKotlinTask = project.tasks.findByName("compileKotlin")
        if (compileKotlinTask == null) {
            val msg = "Unable to find the task kotlinCompile and" +
                " register the dependency kotlinCompile -> fabriktGenerate."
            project.logger.info(msg)
        } else {
            compileKotlinTask.dependsOn(task)
        }
    }

    private fun <T, P : Property<T>> P.setIfPresent(value: P) {
        if (value.isPresent) set(value)
    }

    private fun Project.addGeneratedDirectoriesToIdea(extension: FabriktExtension) {
        this.afterEvaluate { project ->
            val idea = project.extensions.findByType(IdeaModel::class.java)
            if (idea != null) {
                extension.map { project.createGenerateTaskConfiguration(it) }.forEach { config ->
                    val dir = config.outputDirectory.dir(config.sourcesPath).get().asFile
                    idea.module.generatedSourceDirs.add(dir)
                }
            }
        }
    }

}
