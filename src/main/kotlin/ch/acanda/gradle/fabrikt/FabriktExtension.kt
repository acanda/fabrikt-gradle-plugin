package ch.acanda.gradle.fabrikt

import com.cjbooms.fabrikt.cli.CodeGenerationType
import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.SetProperty
import java.io.File
import javax.inject.Inject

open class FabriktExtension @Inject constructor(objects: ObjectFactory) :
    GenerateExtensionContainer by objects.generateExtensionContainer() {

    fun generate(name: String, action: Action<FabriktGenerateExtension>) {
        register(name, action)
    }

}

@Suppress("TooManyFunctions")
open class FabriktGenerateExtension @Inject constructor(
    private val name: String,
    private val objects: ObjectFactory
) : Named {

    override fun getName(): String = name

    val apiFile: RegularFileProperty = objects.fileProperty()
    val basePackage: Property<CharSequence> = objects.property(CharSequence::class.java)
    val outputDirectory: DirectoryProperty = objects.directoryProperty()
    val targets: SetProperty<CodeGenerationType> = objects.setProperty(CodeGenerationType::class.java)
        .convention(null as Set<CodeGenerationType>?)

    // ---------------------------------------------------------------------- //
    // Syntactic sugar for the property apiFile                               //
    // ---------------------------------------------------------------------- //
    fun apiFile(file: File) = apiFile.set(file)

    @JvmName("apiFileFromFileProvider")
    fun apiFile(file: Provider<File>) = apiFile.set(objects.fileProperty().fileProvider(file))
    fun apiFile(file: CharSequence) = apiFile.set(File(file.toString()))

    @JvmName("apiFileFromCharSequenceProvider")
    fun apiFile(file: Provider<out CharSequence>) =
        apiFile.set(objects.fileProperty().fileProvider(file.map { File(it.toString()) }))

    fun apiFile(file: RegularFile) = apiFile.set(file)

    @JvmName("apiFileFromRegularFileProvider")
    fun apiFile(file: Provider<RegularFile>) = apiFile.set(file)

    // ---------------------------------------------------------------------- //
    // Syntactic sugar for the property basePackage                           //
    // ---------------------------------------------------------------------- //
    fun basePackage(basePackage: CharSequence) = this.basePackage.set(basePackage)

    fun basePackage(basePackage: Provider<out CharSequence>) = this.basePackage.set(basePackage)

    // ---------------------------------------------------------------------- //
    // Syntactic sugar for the property outputDirectory                       //
    // ---------------------------------------------------------------------- //
    fun outputDirectory(file: File) = outputDirectory.set(file)

    @JvmName("outputDirectoryFromFileProvider")
    fun outputDirectory(file: Provider<File>) = outputDirectory.set(objects.directoryProperty().fileProvider(file))

    fun outputDirectory(file: CharSequence) = outputDirectory.set(File(file.toString()))

    @JvmName("outputDirectoryFromCharSequenceProvider")
    fun outputDirectory(file: Provider<out CharSequence>) =
        outputDirectory.set(objects.directoryProperty().fileProvider(file.map { File(it.toString()) }))

    fun outputDirectory(directory: Directory) = outputDirectory.set(directory)

    @JvmName("outputDirectoryFromDirectoryProvider")
    fun outputDirectory(directory: Provider<Directory>) = outputDirectory.set(directory)

    // ---------------------------------------------------------------------- //
    // Syntactic sugar for the property targets                               //
    // ---------------------------------------------------------------------- //
    fun targets(vararg targets: CodeGenerationType) = this.targets.set(targets.toSet())

    fun targets(targets: Iterable<CodeGenerationType>) = this.targets.set(targets.toSet())

    fun targets(targets: Provider<out Iterable<CodeGenerationType>>) = this.targets.set(targets)

    @Suppress("VariableNaming", "unused")
    val HTTP_MODELS: CodeGenerationType = CodeGenerationType.HTTP_MODELS

    @Suppress("VariableNaming", "unused")
    val CONTROLLERS: CodeGenerationType = CodeGenerationType.CONTROLLERS

    @Suppress("VariableNaming", "unused")
    val CLIENT: CodeGenerationType = CodeGenerationType.CLIENT

    @Suppress("VariableNaming", "unused")
    val QUARKUS_REFLECTION_CONFIG: CodeGenerationType = CodeGenerationType.QUARKUS_REFLECTION_CONFIG

}

private typealias GenerateExtensionContainer = NamedDomainObjectContainer<FabriktGenerateExtension>

private fun ObjectFactory.generateExtensionContainer() =
    this.domainObjectContainer(FabriktGenerateExtension::class.java)
