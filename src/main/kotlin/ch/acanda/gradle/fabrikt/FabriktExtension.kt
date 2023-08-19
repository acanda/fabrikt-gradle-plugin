package ch.acanda.gradle.fabrikt

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property

interface FabriktExtension {

    val apiFile: RegularFileProperty
    val basePackage: Property<String>
    val outputDirectory: DirectoryProperty

}
