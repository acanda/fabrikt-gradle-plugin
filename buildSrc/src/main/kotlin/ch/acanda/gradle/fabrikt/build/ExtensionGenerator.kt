package ch.acanda.gradle.fabrikt.build

import com.cjbooms.fabrikt.cli.CodeGenerationType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.WildcardTypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.jvm.jvmName
import org.gradle.api.Action
import org.gradle.api.DefaultTask
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
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.file.Path
import javax.inject.Inject
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties


@CacheableTask
abstract class ExtensionGenerator : DefaultTask() {

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun generate() {
        val fabriktGenerateExtensionName = ClassName(PACKAGE, "FabriktGenerateExtension")
        val fabriktGenerateExtension = getFabriktGenerateExtension(fabriktGenerateExtensionName)
        val fabriktGenerateName = ClassName(PACKAGE, "FabriktExtension")
        val fabriktExtension = fabriktExtension(fabriktGenerateName, fabriktGenerateExtensionName)

        val file = FileSpec.builder(fabriktGenerateName)
            .addType(fabriktExtension)
            .addType(fabriktGenerateExtension)
            .build()

        file.writeTo(outputDirectory.get().asFile)
    }

    internal companion object {

        private const val PACKAGE = "ch.acanda.gradle.fabrikt"
        private const val PROP_NAME = "name"
        private const val PROP_OBJECTS = "objects"

        internal fun getFabriktGenerateExtension(className: ClassName) = TypeSpec.classBuilder(className)
            .addModifiers(KModifier.OPEN)
            .named()
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addAnnotation(Inject::class)
                    .addParameter(PROP_NAME, String::class)
                    .addParameter(PROP_OBJECTS, ObjectFactory::class)
                    .build()
            )
            .addProperty(
                PropertySpec.builder(PROP_OBJECTS, ObjectFactory::class)
                    .addModifiers(KModifier.PRIVATE)
                    .initializer(PROP_OBJECTS)
                    .build()
            )
            .fileProperty("apiFile")
            .stringProperty("basePackage")
            .directoryProperty("outputDirectory")
            .enumSetProperty("targets", CodeGenerationType::class)
            .build()

        internal fun fabriktExtension(className: ClassName, valueType: TypeName) = TypeSpec.classBuilder(className)
            .addModifiers(KModifier.OPEN)
            .addSuperinterface(
                namedContainer(valueType),
                CodeBlock.of("%N.domainObjectContainer(%T::class.java)", PROP_OBJECTS, valueType)
            )
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addAnnotation(Inject::class)
                    .addParameter(PROP_OBJECTS, ObjectFactory::class)
                    .build()
            )
            .addProperty(
                PropertySpec.builder(PROP_OBJECTS, ObjectFactory::class)
                    .addModifiers(KModifier.PRIVATE)
                    .initializer(PROP_OBJECTS)
                    .build()
            )
            .addFunction(
                FunSpec.builder("generate")
                    .addParameter("name", String::class)
                    .addParameter("action", Action::class.asTypeName().parameterizedBy(valueType))
                    .addStatement("register(name, action)")
                    .build()
            )
            .build()


        internal fun TypeSpec.Builder.named() = apply {
            addSuperinterface(Named::class)
            addProperty(
                PropertySpec.builder(PROP_NAME, String::class)
                    .addModifiers(KModifier.PRIVATE)
                    .initializer(PROP_NAME)
                    .build()
            )
            addFunction(
                FunSpec.builder("getName")
                    .addModifiers(KModifier.OVERRIDE)
                    .returns(String::class)
                    .addStatement("return %N", PROP_NAME)
                    .build()
            )
        }

        internal fun TypeSpec.Builder.stringProperty(name: String) = apply {
            addProperty(
                PropertySpec.builder(name, Property::class.parameterizedBy(CharSequence::class))
                    .initializer("%N.property(CharSequence::class.java)", PROP_OBJECTS)
                    .build()
            )
            addFunction(
                FunSpec.builder(name)
                    .addParameter(name, CharSequence::class)
                    .addStatement("this.%1N.set(%1N)", name)
                    .build()
            )
            addFunction(
                FunSpec.builder(name)
                    .addParameter(name, provider<CharSequence>())
                    .addStatement("this.%1N.set(%1N)", name)
                    .build()
            )
        }

        internal fun TypeSpec.Builder.fileProperty(name: String) = apply {
            addProperty(
                PropertySpec.builder(name, RegularFileProperty::class)
                    .initializer("%N.fileProperty()", PROP_OBJECTS)
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
                    .addStatement("this.%1N.set(%2N.fileProperty().fileProvider(%1N))", name, PROP_OBJECTS)
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
                        PROP_OBJECTS
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
                        PROP_OBJECTS,
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

        internal fun TypeSpec.Builder.directoryProperty(name: String) = apply {
            addProperty(
                PropertySpec.builder(name, DirectoryProperty::class)
                    .initializer("%N.directoryProperty()", PROP_OBJECTS)
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
                    .addStatement("this.%1N.set(%2N.directoryProperty().fileProvider(%1N))", name, PROP_OBJECTS)
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
                        PROP_OBJECTS
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
                        PROP_OBJECTS,
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

        internal fun TypeSpec.Builder.enumSetProperty(name: String, enumType: KClass<out Enum<*>>) = apply {
            addProperty(
                PropertySpec.builder(name, SetProperty::class.parameterizedBy(enumType))
                    .initializer(
                        "%1N.setProperty(%2T::class.java).convention(null·as·Set<%2T>?)",
                        PROP_OBJECTS,
                        enumType
                    )
                    .build()
            )
            addFunction(
                FunSpec.builder(name)
                    .addParameter(name, enumType, KModifier.VARARG)
                    .addStatement("this.%1N.set(%1N.toSet())", name)
                    .build()
            )
            addFunction(
                FunSpec.builder(name)
                    .addParameter(name, Iterable::class.parameterizedBy(enumType))
                    .addStatement("this.%1N.set(%1N.toSet())", name)
                    .build()
            )
            addFunction(
                FunSpec.builder(name)
                    .addParameter(name, provider(Iterable::class.parameterizedBy(enumType)))
                    .addStatement("this.%1N.set(%1N)", name)
                    .build()
            )
            enumType.java.enumConstants.iterator().forEach { enumValue ->
                val spec = PropertySpec.builder(enumValue.name, enumType).initializer("%T.%N", enumType, enumValue.name)
                enumValue::class.memberProperties
                    .find { it.name == "description" }
                    ?.let {
                        spec.addKdoc(it.getter.call(enumValue) as String)
                    }
                addProperty(spec.build())
            }
        }

        private fun namedContainer(valueType: TypeName) =
            NamedDomainObjectContainer::class.asClassName().parameterizedBy(valueType)

        private inline fun <reified T> provider() = provider(T::class.asTypeName())

        private fun provider(valueType: TypeName) =
            Provider::class.asClassName().parameterizedBy(
                when (valueType) {
                    File::class.asTypeName() -> valueType
                    else -> WildcardTypeName.producerOf(valueType)
                }
            )

    }

}
