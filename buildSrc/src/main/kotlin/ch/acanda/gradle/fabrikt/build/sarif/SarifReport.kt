package ch.acanda.gradle.fabrikt.build.sarif

import io.github.detekt.sarif4k.ReportingDescriptor
import io.github.detekt.sarif4k.Result
import io.github.detekt.sarif4k.Run
import io.github.detekt.sarif4k.SarifSchema210
import io.github.detekt.sarif4k.Tool
import io.github.detekt.sarif4k.ToolComponent
import io.github.detekt.sarif4k.Version
import kotlinx.serialization.json.Json
import java.nio.file.Path
import kotlin.io.path.writeText

public class SarifReport(
    private val toolName: String,
    private val rules: List<ReportingDescriptor> = emptyList(),
    private val results: List<Result>,
) {

    fun writeTo(file: Path) {
        file.writeText(Json.encodeToString(buildSarif()))
    }

    private fun buildSarif() = SarifSchema210(
        schema = "http://json.schemastore.org/sarif-2.1.0",
        version = Version.The210,
        runs = buildRuns()
    )

    private fun buildRuns() = listOf(
        Run(
            tool = buildTool(),
            results = results
        )
    )

    private fun buildTool() = Tool(
        driver = ToolComponent(name = toolName, rules = rules),
    )

}
