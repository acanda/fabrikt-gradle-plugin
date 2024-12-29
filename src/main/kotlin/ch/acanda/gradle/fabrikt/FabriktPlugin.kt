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

        @Suppress("MaxLineLength")
        /**
         * We use the same task group name as the
         * [OpenAPI Generator Gradle Plugin](https://github.com/OpenAPITools/openapi-generator/blob/master/modules/openapi-generator-gradle-plugin/src/main/kotlin/org/openapitools/generator/gradle/plugin/OpenApiGeneratorPlugin.kt#L162),
         * so the tasks of this plugin are available in the same group as the
         * tasks of the OpenAPI Generator Gradle Plugin.
         */
        const val TASK_GROUP = "OpenAPI Tools"

        /**
         * Creates a suffix for a task name using the following rules:
         * - The first character and characters following a non-alphanumeric
         *   character are converted to upper case.
         * - Non-alphanumeric characters are removed.
         *
         * For example, the configuration name "dog-api" will be converted to
         * the task name suffix "DogApi".
         */
        internal fun String.toTaskNameSuffix(): String {
            val builder = StringBuilder(length)
            var isNextUpperCase = true
            for (cp in codePoints()) {
                if (Character.isLetterOrDigit(cp)) {
                    if (isNextUpperCase) {
                        builder.appendCodePoint(Character.toUpperCase(cp))
                        isNextUpperCase = false
                    } else {
                        builder.appendCodePoint(cp)
                    }
                } else {
                    isNextUpperCase = true
                }
            }
            return builder.toString()
        }

    }

    override fun apply(project: Project) {
        val extension = project.extensions.create("fabrikt", FabriktExtension::class.java)
        val fabriktGenerateTask = project.registerGenerateTask(extension)
        project.registerGenerateNamedTasks()
        project.addCompileKotlinDependsOn(fabriktGenerateTask)
        project.addGeneratedDirectoriesToIdea(extension)
    }

    private fun Project.registerGenerateTask(extension: FabriktExtension): TaskProvider<FabriktGenerateTask> =
        tasks.register("fabriktGenerate", FabriktGenerateTask::class.java) { task ->
            task.group = TASK_GROUP
            task.description = "Generates the classes for all Fabrikt configurations."
            val configurations = extension.getGenerateExtensions()
            val defaults = extension.getDefaults()
            task.addConfigurations(configurations, defaults, initializeGenerateTaskConfiguration())
            project.addOutputDirectoryToKotlinSourceSet(extension)
        }

    private fun Project.registerGenerateNamedTasks() {
        afterEvaluate { project ->
            project.extensions.findByType(FabriktExtension::class.java)?.let { extension ->
                extension.names.map { name ->
                    val suffix = name.toTaskNameSuffix()
                    if (suffix.isNotBlank()) {
                        tasks.register("fabriktGenerate$suffix", FabriktGenerateTask::class.java) { task ->
                            task.group = TASK_GROUP
                            val configurations =
                                extension.getGenerateExtension(name).map { listOf(it) }
                            val apiFile = configurations.get().first().apiFile.get().asFile
                                .relativeTo(project.layout.projectDirectory.asFile)
                            task.description = "Generates the classes for $apiFile."
                            val defaults = extension.getDefaults()
                            task.addConfigurations(
                                configurations,
                                defaults,
                                initializeGenerateTaskConfiguration { skip.set(false) }
                            )
                            project.addOutputDirectoryToKotlinSourceSet(extension)
                        }
                    }
                }
            }
        }
    }

    private fun Project.addOutputDirectoryToKotlinSourceSet(extension: FabriktExtension) {
        val sourceSets = extensions.findByName("sourceSets") as SourceSetContainer?
        val srcDirSet = sourceSets
            ?.findByName("main")
            ?.extensions
            ?.findByName("kotlin") as SourceDirectorySet?

        if (srcDirSet == null) {
            val msg = "Unable to find the source set \"main/kotlin\" and add the Fabrikt output directories."
            logger.info(msg)
        } else {
            val defaults = extension.getDefaults()
            val srcDirs = extension.getGenerateExtensions().map { configs ->
                configs.map { config ->
                    val outputDirectory = config.outputDirectory
                        .getOrElse(defaults.flatMap { it.outputDirectory }.get())
                    val sourcesPath = config.sourcesPath
                        .getOrElse(defaults.flatMap { it.sourcesPath }.get()).toString()
                    outputDirectory.dir(sourcesPath)
                }
            }
            srcDirSet.srcDirs(srcDirs)
        }
    }

    private fun Project.addCompileKotlinDependsOn(task: TaskProvider<FabriktGenerateTask>) {
        val compileKotlinTask = tasks.findByName("compileKotlin")
        if (compileKotlinTask == null) {
            val msg = "Unable to find the task kotlinCompile and" +
                " register the dependency kotlinCompile -> fabriktGenerate."
            logger.info(msg)
        } else {
            compileKotlinTask.dependsOn(task)
        }
    }

    private fun Project.addGeneratedDirectoriesToIdea(extension: FabriktExtension) {
        afterEvaluate { project ->
            val idea = project.extensions.findByType(IdeaModel::class.java)
            if (idea != null) {
                val defaults = extension.getDefaults()
                extension.getGenerateExtensions().get().forEach { config ->
                    val outputDirectory = config.outputDirectory
                        .getOrElse(defaults.flatMap { it.outputDirectory }.get())
                    val sourcesPath = config.sourcesPath
                        .getOrElse(defaults.flatMap { it.sourcesPath }.get())
                    val generatedSourcesDir = outputDirectory.dir(sourcesPath.toString()).asFile
                    logger.debug("Adding {} as a generated source directory to IDEA.", generatedSourcesDir)
                    idea.module.generatedSourceDirs.add(generatedSourcesDir)
                }
            }
        }
    }

}
