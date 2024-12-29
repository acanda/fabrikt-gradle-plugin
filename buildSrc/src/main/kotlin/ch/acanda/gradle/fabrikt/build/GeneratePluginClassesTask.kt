package ch.acanda.gradle.fabrikt.build

import ch.acanda.gradle.fabrikt.build.builder.buildConfigurations
import ch.acanda.gradle.fabrikt.build.builder.buildDefaults
import ch.acanda.gradle.fabrikt.build.builder.buildExtensions
import ch.acanda.gradle.fabrikt.build.builder.buildOptions
import ch.acanda.gradle.fabrikt.build.builder.buildInitializers
import ch.acanda.gradle.fabrikt.build.schema.ConfigurationSchema
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

@CacheableTask
abstract class GeneratePluginClassesTask : DefaultTask() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val schema: RegularFileProperty

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun generate() {
        val outputDir = outputDirectory.get().asFile
        val schema: ConfigurationSchema =
            ObjectMapper(YAMLFactory()).registerKotlinModule().readValue(schema.get().asFile)
        buildOptions(schema.options).writeTo(outputDir)
        buildConfigurations(schema).writeTo(outputDir)
        buildExtensions(schema).writeTo(outputDir)
        buildDefaults(schema).writeTo(outputDir)
        buildInitializers(schema).writeTo(outputDir)
    }

}
