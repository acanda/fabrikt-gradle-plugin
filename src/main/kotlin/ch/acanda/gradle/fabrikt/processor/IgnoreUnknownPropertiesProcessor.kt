package ch.acanda.gradle.fabrikt.processor

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.extension
import kotlin.io.path.isRegularFile
import kotlin.io.path.walk

@OptIn(ExperimentalPathApi::class)
fun addIgnoreUnknownPropertiesAnnotation(modelsDirectory: Path) {
    modelsDirectory.walk()
        .filter { it.isRegularFile() }
        .filter { it.extension == "kt" }
        .forEach { addAnnotation(it) }
}

private fun addAnnotation(path: Path) {
    // Exclusively lock the file to prevent concurrent writes.
    FileChannel.open(path, StandardOpenOption.READ, StandardOpenOption.WRITE).use { channel ->
        channel.lock().use {
            addAnnotation(channel)
        }
    }
}

private fun addAnnotation(channel: FileChannel) {
    val import = "import ${JsonIgnoreProperties::class.qualifiedName}".replace("annotation", "`annotation`")
    val annotation =
        "@${JsonIgnoreProperties::class.simpleName}(${JsonIgnoreProperties::ignoreUnknown.name} = true)"
    val dataClass = "public data class"
    val buffer = ByteBuffer.allocate(channel.size().toInt())
    channel.read(buffer)
    val content = String(buffer.array(), Charsets.UTF_8)
    if (content.contains(annotation) || !content.contains(dataClass)) {
        return
    }
    val annotatedClass = content
        .replaceFirst("import", "$import\nimport")
        .replace(dataClass, "$annotation\npublic data class")
    channel.position(0)
    channel.write(ByteBuffer.wrap(annotatedClass.toByteArray()))
    channel.force(true)
}
