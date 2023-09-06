package ch.acanda.gradle.fabrikt.build

import ch.acanda.gradle.fabrikt.build.generator.directoryProperty
import ch.acanda.gradle.fabrikt.build.generator.enumProperty
import ch.acanda.gradle.fabrikt.build.generator.enumSetProperty
import ch.acanda.gradle.fabrikt.build.generator.fileProperty
import ch.acanda.gradle.fabrikt.build.generator.filesProperty
import ch.acanda.gradle.fabrikt.build.generator.named
import ch.acanda.gradle.fabrikt.build.generator.stringProperty
import com.cjbooms.fabrikt.cli.ClientCodeGenOptionType
import com.cjbooms.fabrikt.cli.ClientCodeGenTargetType
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
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

@CacheableTask
abstract class ExtensionGenerator : DefaultTask() {

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun generate() {
        val fabriktGenerateExtensionName = ClassName(PACKAGE, "FabriktGenerateExtension")
        val fabriktGenerateExtension = fabriktGenerateExtension(fabriktGenerateExtensionName)
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
        internal const val PROP_NAME = "name"
        internal const val PROP_OBJECTS = "objects"

        internal fun fabriktGenerateExtension(className: ClassName) = TypeSpec.classBuilder(className)
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
            .filesProperty("apiFragments")
            .stringProperty("basePackage")
            .directoryProperty("outputDirectory")
            .enumSetProperty("targets", CodeGenerationType::class)
            .enumSetProperty("httpClientOpts", ClientCodeGenOptionType::class)
            .enumProperty("httpClientTarget", ClientCodeGenTargetType::class)
            .build()

        internal fun fabriktExtension(className: ClassName, valueType: TypeName) = TypeSpec.classBuilder(className)
            .addModifiers(KModifier.OPEN)
            .addSuperinterface(
                NamedDomainObjectContainer::class.asClassName().parameterizedBy(valueType),
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

    }

}
