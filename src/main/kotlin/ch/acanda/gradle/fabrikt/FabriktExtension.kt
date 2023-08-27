package ch.acanda.gradle.fabrikt

import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

open class FabriktExtension @Inject constructor(objects: ObjectFactory) :
    GenerateExtensionContainer by objects.generateExtensionContainer() {

    fun generate(name: String, action: Action<FabriktGenerateExtension>) {
        register(name, action)
    }

}

open class FabriktGenerateExtension @Inject constructor(
    private val name: String,
    objects: ObjectFactory
) : Named {

    override fun getName(): String = name

    val apiFile: RegularFileProperty = objects.fileProperty()
    val basePackage: Property<String> = objects.property(String::class.java)
    val outputDirectory: DirectoryProperty = objects.directoryProperty()

}

private typealias GenerateExtensionContainer = NamedDomainObjectContainer<FabriktGenerateExtension>

private fun ObjectFactory.generateExtensionContainer() =
    this.domainObjectContainer(FabriktGenerateExtension::class.java)
