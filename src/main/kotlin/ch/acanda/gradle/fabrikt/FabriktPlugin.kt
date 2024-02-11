package ch.acanda.gradle.fabrikt

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.plugins.ide.idea.model.IdeaModel

class FabriktPlugin : Plugin<Project> {

    companion object {
        const val PLUGIN_ID = "ch.acanda.gradle.fabrikt"
    }

    override fun apply(project: Project) {
        val extension = project.extensions.create("fabrikt", FabriktExtension::class.java)

        val fabriktGenerateTask = project.tasks.register("fabriktGenerate", FabriktGenerateTask::class.java) { task ->
            task.group = "OpenAPI Tools"
            task.configurations.set(extension)
            project.addOutputDirectoryToKotlinSourceSet(extension)
        }

        project.addCompileKotlinDependsOn(fabriktGenerateTask)
        project.addGeneratedDirectoriesToIdea(extension)
    }

    private fun Project.addOutputDirectoryToKotlinSourceSet(configurations: Collection<GenerateTaskConfiguration>) {
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

    private fun Project.addGeneratedDirectoriesToIdea(configurations: Collection<GenerateTaskConfiguration>) {
        this.afterEvaluate { project ->
            val idea = project.extensions.findByType(IdeaModel::class.java)
            if (idea != null) {
                configurations.forEach { config ->
                    val dir = config.outputDirectory.dir(config.sourcesPath).get().asFile
                    idea.module.generatedSourceDirs.add(dir)
                }
            }
        }
    }

}
