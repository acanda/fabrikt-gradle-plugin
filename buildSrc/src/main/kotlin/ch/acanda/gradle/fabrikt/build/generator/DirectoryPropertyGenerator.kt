package ch.acanda.gradle.fabrikt.build.generator

import ch.acanda.gradle.fabrikt.build.ExtensionGenerator
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.jvm.jvmName
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import java.io.File
import java.nio.file.Path

@Suppress("LongMethod")
internal fun TypeSpec.Builder.directoryProperty(name: String) = apply {
    addProperty(
        PropertySpec.builder(name, DirectoryProperty::class)
            .initializer("%N.directoryProperty()", ExtensionGenerator.PROP_OBJECTS)
            .build()
    )
    addFunction(
        FunSpec.builder(name)
            .addParameter(name, File::class)
            .addStatement("this.%1N.set(%1N)", name)
            .build()
    )
    addFunction(
        FunSpec.builder(name)
            .jvmName("${name}FromFileProvider")
            .addParameter(name, provider<File>())
            .addStatement(
                "this.%1N.set(%2N.directoryProperty().fileProvider(%1N))",
                name,
                ExtensionGenerator.PROP_OBJECTS
            )
            .build()
    )
    addFunction(
        FunSpec.builder(name)
            .addParameter(name, Path::class)
            .addStatement("this.%1N.set(%1N.toFile())", name)
            .build()
    )
    addFunction(
        FunSpec.builder(name)
            .jvmName("${name}FromPathProvider")
            .addParameter(name, provider<Path>())
            .addStatement(
                "this.%1N.set(%2N.directoryProperty().fileProvider(%1N.map { it.toFile() }))",
                name,
                ExtensionGenerator.PROP_OBJECTS
            )
            .build()
    )
    addFunction(
        FunSpec.builder(name)
            .addParameter(name, CharSequence::class)
            .addStatement("this.%1N.set(%2T(%1N.toString()))", name, File::class)
            .build()
    )
    addFunction(
        FunSpec.builder(name)
            .jvmName("${name}FromCharSequenceProvider")
            .addParameter(name, provider<CharSequence>())
            .addStatement(
                "val provider = %N.directoryProperty().fileProvider(%N.map·{ %T(it.toString()) })",
                ExtensionGenerator.PROP_OBJECTS,
                name,
                File::class
            )
            .addStatement("this.%N.set(provider)", name)
            .build()
    )
    addFunction(
        FunSpec.builder(name)
            .addParameter(name, Directory::class)
            .addStatement("this.%1N.set(%1N)", name)
            .build()
    )
    addFunction(
        FunSpec.builder(name)
            .jvmName("${name}FromDirectoryProvider")
            .addParameter(name, provider<Directory>())
            .addStatement("this.%1N.set(%1N)", name)
            .build()
    )
}
