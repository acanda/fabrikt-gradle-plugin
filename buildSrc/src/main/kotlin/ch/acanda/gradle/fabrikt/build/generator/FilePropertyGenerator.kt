package ch.acanda.gradle.fabrikt.build.generator

import ch.acanda.gradle.fabrikt.build.ExtensionGenerator
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.jvm.jvmName
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import java.io.File
import java.nio.file.Path

internal fun TypeSpec.Builder.fileProperty(name: String) = apply {
    addProperty(
        PropertySpec.builder(name, RegularFileProperty::class)
            .initializer("%N.fileProperty()", ExtensionGenerator.PROP_OBJECTS)
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
            .addStatement("this.%1N.set(%2N.fileProperty().fileProvider(%1N))", name, ExtensionGenerator.PROP_OBJECTS)
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
                "this.%1N.set(%2N.fileProperty().fileProvider(%1N.map { it.toFile() }))",
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
                "val provider = %N.fileProperty().fileProvider(%N.map·{ %T(it.toString()) })",
                ExtensionGenerator.PROP_OBJECTS,
                name,
                File::class
            )
            .addStatement("this.%N.set(provider)", name)
            .build()
    )
    addFunction(
        FunSpec.builder(name)
            .addParameter(name, RegularFile::class)
            .addStatement("this.%1N.set(%1N)", name)
            .build()
    )
    addFunction(
        FunSpec.builder(name)
            .jvmName("${name}FromRegularFileProvider")
            .addParameter(name, provider<RegularFile>())
            .addStatement("this.%1N.set(%1N)", name)
            .build()
    )
}
