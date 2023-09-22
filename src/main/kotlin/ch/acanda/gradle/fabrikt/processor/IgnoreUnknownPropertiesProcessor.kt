package ch.acanda.gradle.fabrikt.processor

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.extension
import kotlin.io.path.isRegularFile
import kotlin.io.path.readText
import kotlin.io.path.walk
import kotlin.io.path.writeText

@OptIn(ExperimentalPathApi::class)
fun addIgnoreUnknownPropertiesAnnotation(modelsDirectory: Path) {
    modelsDirectory.walk()
        .filter { it.isRegularFile() }
        .filter { it.extension == "kt" }
        .forEach { addAnnotation(it) }
}

private fun addAnnotation(file: Path) {
    val import = "import ${JsonIgnoreProperties::class.qualifiedName}"
    val annotation = "${JsonIgnoreProperties::class.simpleName}(${JsonIgnoreProperties::ignoreUnknown.name} = true)"
    val annotatedClass = file.readText()
        .replaceFirst("import", "$import\nimport")
        .replaceFirst("data class", "$annotation\ndata class")
    file.writeText(annotatedClass)
}
