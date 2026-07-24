package ch.acanda.gradle.fabrikt.build

import ch.acanda.gradle.fabrikt.build.builder.isNested
import ch.acanda.gradle.fabrikt.build.schema.ConfigurationSchema
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.VerificationException
import org.gradle.language.base.plugins.LifecycleBasePlugin

abstract class CheckReadmeTask : DefaultTask() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val schema: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val readme: RegularFileProperty

    init {
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        description = "Checks that all properties are documented in README.md."
        readme.convention(project.layout.projectDirectory.file("README.md"))
    }

    @TaskAction
    fun performCheck() {
        val schema: ConfigurationSchema =
            ObjectMapper(YAMLFactory()).registerKotlinModule().readValue(schema.get().asFile)
        val readme = readme.get().asFile.readText()

        val undocumentedProperties =
            schema.configurations
                .flatMap { (name, config) ->
                    config.properties
                        .filter { (_, definition) -> !definition.isNested(schema.configurations) }
                        .map { (property, _) -> "${PREFIX[name]}$property" }
                }
                .filter { property -> !readme.contains("| $property ") }
                .toList();

        if (!undocumentedProperties.isEmpty()) {
            val msg = if (undocumentedProperties.size == 1) {
                val property = undocumentedProperties.first()
                "The property $property is not documented in README.md."
            } else {
                "The following properties are not documented in README.md:\n- " +
                    undocumentedProperties.joinToString("\n- ")
            }
            logger.error(msg)
            throw VerificationException(msg)
        }
    }

    private companion object {
        private val PREFIX = mapOf(
            "GenerateTask" to "",
            "TypeOverrides" to "typeOverrides.",
            "GenerateClient" to "client.",
            "GenerateController" to "controller.",
            "GenerateModel" to "model.",
        )
    }

}
