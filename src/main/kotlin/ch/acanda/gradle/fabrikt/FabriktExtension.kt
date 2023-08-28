package ch.acanda.gradle.fabrikt

import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import java.io.File
import javax.inject.Inject

open class FabriktExtension @Inject constructor(objects: ObjectFactory) :
    GenerateExtensionContainer by objects.generateExtensionContainer() {

    fun generate(name: String, action: Action<FabriktGenerateExtension>) {
        register(name, action)
    }

}

open class FabriktGenerateExtension @Inject constructor(
    private val name: String,
    private val objects: ObjectFactory
) : Named {

    override fun getName(): String = name

    val apiFile: RegularFileProperty = objects.fileProperty()
    val basePackage: Property<String> = objects.property(String::class.java)
    val outputDirectory: DirectoryProperty = objects.directoryProperty()

    fun apiFile(file: File) = apiFile.set(file)

    @JvmName("apiFileFromFileProvider")
    fun apiFile(file: Provider<File>) = apiFile.set(objects.fileProperty().fileProvider(file))
    fun apiFile(file: String) = apiFile.set(File(file))

    @JvmName("apiFileFromStringProvider")
    fun apiFile(file: Provider<String>) = apiFile.set(objects.fileProperty().fileProvider(file.map { File(it) }))
    fun apiFile(file: RegularFile) = apiFile.set(file)

    @JvmName("apiFileFromRegularFileProvider")
    fun apiFile(file: Provider<RegularFile>) = apiFile.set(file)

}

private typealias GenerateExtensionContainer = NamedDomainObjectContainer<FabriktGenerateExtension>

private fun ObjectFactory.generateExtensionContainer() =
    this.domainObjectContainer(FabriktGenerateExtension::class.java)
