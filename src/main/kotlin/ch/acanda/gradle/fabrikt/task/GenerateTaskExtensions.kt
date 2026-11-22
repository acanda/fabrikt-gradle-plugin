package ch.acanda.gradle.fabrikt.task

import ch.acanda.gradle.fabrikt.GenerateTaskConfiguration

fun GenerateTaskConfiguration.getSpecName(): String {
    fun apiFileName() = apiFile.orNull?.asFile?.name
    fun remoteApiFileName() = remoteApiFile.apiFile.orNull?.path?.split(Regex("[/\\\\]"))?.lastOrNull()
    fun jsonFileName() = jsonSchema.file.orNull?.asFile?.name

    @Suppress("detekt:UnreachableCode")
    val filename = apiFileName() ?: remoteApiFileName() ?: jsonFileName()
    return name + (filename?.let { " ($it)" } ?: "")
}
