package ch.acanda.gradle.fabrikt.build

import ch.acanda.gradle.fabrikt.build.builder.isNested
import ch.acanda.gradle.fabrikt.build.sarif.SarifReport
import ch.acanda.gradle.fabrikt.build.schema.ConfigurationSchema
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.github.detekt.sarif4k.ArtifactLocation
import io.github.detekt.sarif4k.Level
import io.github.detekt.sarif4k.Location
import io.github.detekt.sarif4k.Message
import io.github.detekt.sarif4k.MultiformatMessageString
import io.github.detekt.sarif4k.PhysicalLocation
import io.github.detekt.sarif4k.PropertyBag
import io.github.detekt.sarif4k.Region
import io.github.detekt.sarif4k.ReportingConfiguration
import io.github.detekt.sarif4k.ReportingDescriptor
import io.github.detekt.sarif4k.Result
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.VerificationException
import org.gradle.api.tasks.VerificationTask
import org.gradle.language.base.plugins.LifecycleBasePlugin
import java.io.File

@CacheableTask
abstract class CheckReadmeTask : DefaultTask(), VerificationTask {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val schema: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val readme: RegularFileProperty

    @get:OutputFile
    abstract val sarif: RegularFileProperty

    init {
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        description = "Checks that all properties are documented in README.md."
        readme.convention(project.layout.projectDirectory.file("README.md"))
        sarif.convention(project.layout.buildDirectory.file("reports/fabrikt-gradle-plugin/report.sarif"))
    }

    @TaskAction
    fun performCheck() {
        val schema: ConfigurationSchema =
            ObjectMapper(YAMLFactory()).registerKotlinModule().readValue(schema.get().asFile)
        val readmeFile = readme.get().asFile
        val readme = readmeFile.readText()

        val undocumentedProperties =
            schema.configurations
                .flatMap { (name, config) ->
                    config.properties
                        .filter { (_, definition) -> !definition.isNested(schema.configurations) }
                        .map { (property, _) -> "${PREFIX[name]}$property" }
                }
                .filter { property -> !readme.contains("| $property ") }
                .toList();

        createSarifReport(readmeFile, readme, undocumentedProperties)

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

    private fun createSarifReport(
        readmeFile: File,
        readme: String,
        undocumentedProperties: List<String>
    ) {
        val report = SarifReport(
            toolName = "fabrikt-gradle-plugin",
            rules = buildRules(),
            results = undocumentedProperties.map { property ->
                Result(
                    ruleID = "UndocumentedProperty",
                    message = Message(
                        text = "The property $property is not documented in README.md.",
                        markdown = "The property `$property` is not documented in README.md."
                    ),
                    locations = listOf(
                        Location(
                            physicalLocation = PhysicalLocation(
                                artifactLocation = ArtifactLocation(uri = readmeFile.toURI().toString()),
                                region = buildRegion(readme, property)
                            )
                        )
                    ),
                    partialFingerprints = mapOf("property" to property)
                )
            },
        )
        report.writeTo(sarif.asFile.get().toPath())
    }

    private fun buildRules(): List<ReportingDescriptor> = listOf(
        ReportingDescriptor(
            id = "UndocumentedProperty",
            shortDescription = MultiformatMessageString(text = "A property is not documented in README.md."),
            defaultConfiguration = ReportingConfiguration(level = Level.Error),
            help = MultiformatMessageString(text = "Document the property in README.md in the table of the section \"Configuration\"."),
            properties = PropertyBag(tags = setOf("documentation"), map = emptyMap())
        )
    )

    private fun buildRegion(readme: String, property: String): Region {
        val table = readme.lineSequence()
            .mapIndexed { index, line -> (index + 1) to line }
            .dropWhile { (_, line) -> line != "## Configuration" }
            .dropWhile { (_, line) -> !line.startsWith("|-") }
            .drop(1)
            .takeWhile { (_, line) -> line.startsWith("| ") }
            .toList()
        var startLine = table.first().first
        var endLine = table.last().first
        if (property.contains('.')) {
            val prefix = property.substringBeforeLast('.')
            val startsWithPrefix: (Pair<Int, String>) -> Boolean = { (_, line) -> line.startsWith("| $prefix.") }
            table.firstOrNull(startsWithPrefix)?.let { startLine = it.first }
            table.lastOrNull(startsWithPrefix)?.let { endLine = it.first }
        }
        return Region(
            startLine = startLine.toLong(),
            endLine = endLine.toLong(),
        )
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
