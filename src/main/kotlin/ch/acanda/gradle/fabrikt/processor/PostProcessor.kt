package ch.acanda.gradle.fabrikt.processor

import ch.acanda.gradle.fabrikt.GenerateTaskConfiguration
import com.cjbooms.fabrikt.model.Destinations
import java.nio.file.Path

fun processGeneratedSources(config: GenerateTaskConfiguration) {
    if (config.model.ignoreUnknownProperties.getOrElse(false)) {
        addIgnoreUnknownPropertiesAnnotation(config.modelDirectory())
    }
}

private fun GenerateTaskConfiguration.modelDirectory(): Path {
    val sourcesPath = sourcesPath.get().toString()
    val modelsPath = Destinations.modelsPackage(basePackage.get().toString()).replace('.', '/')
    return outputDirectory.get().asFile.toPath().resolve(sourcesPath).resolve(modelsPath)
}
