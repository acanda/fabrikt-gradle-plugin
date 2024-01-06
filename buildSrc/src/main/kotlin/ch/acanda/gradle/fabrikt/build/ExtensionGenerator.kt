package ch.acanda.gradle.fabrikt.build

import ch.acanda.gradle.fabrikt.build.generator.ClientTargetOption
import ch.acanda.gradle.fabrikt.build.generator.ControllerTargetOption
import ch.acanda.gradle.fabrikt.build.generator.DateTimeOverrideOption
import ch.acanda.gradle.fabrikt.build.generator.ExternalReferencesResolutionOption
import ch.acanda.gradle.fabrikt.build.generator.FabriktOption
import ch.acanda.gradle.fabrikt.build.generator.ValidationLibraryOption
import ch.acanda.gradle.fabrikt.build.generator.booleanProperty
import ch.acanda.gradle.fabrikt.build.generator.directoryProperty
import ch.acanda.gradle.fabrikt.build.generator.enabledValues
import ch.acanda.gradle.fabrikt.build.generator.enumProperty
import ch.acanda.gradle.fabrikt.build.generator.fileProperty
import ch.acanda.gradle.fabrikt.build.generator.filesProperty
import ch.acanda.gradle.fabrikt.build.generator.named
import ch.acanda.gradle.fabrikt.build.generator.nestedProperty
import ch.acanda.gradle.fabrikt.build.generator.stringProperty
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STAR
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
import javax.annotation.processing.Generated
import javax.inject.Inject
import kotlin.reflect.KClass

@CacheableTask
abstract class ExtensionGenerator : DefaultTask() {

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun generate() {
        val typeOverridesExtensionName = ClassName(PACKAGE, "TypeOverridesExtension")
        val typeOverridesExtension = typeOverridesExtension(typeOverridesExtensionName)
        val clientExtensionName = ClassName(PACKAGE, "ClientExtension")
        val clientExtension = clientExtension(clientExtensionName)
        val controllerExtensionName = ClassName(PACKAGE, "ControllerExtension")
        val controllerExtension = controllerExtension(controllerExtensionName)
        val modelExtensionName = ClassName(PACKAGE, "ModelExtension")
        val modelExtension = modelExtension(modelExtensionName)
        val fabriktGenerateExtensionName = ClassName(PACKAGE, "FabriktGenerateExtension")
        val fabriktGenerateExtension =
            fabriktGenerateExtension(
                fabriktGenerateExtensionName,
                typeOverridesExtensionName,
                clientExtensionName,
                controllerExtensionName,
                modelExtensionName
            )
        val fabriktExtensionName = ClassName(PACKAGE, "FabriktExtension")
        val fabriktExtension = fabriktExtension(fabriktExtensionName, fabriktGenerateExtensionName)

        val file = FileSpec.builder(fabriktExtensionName)
            .addAnnotation(generated())
            .addType(fabriktExtension)
            .addType(fabriktGenerateExtension)
            .addType(typeOverridesExtension)
            .addType(clientExtension)
            .addType(controllerExtension)
            .addType(modelExtension)
            .build()

        file.writeTo(outputDirectory.get().asFile)

        FileSpec.builder(ClassName(PACKAGE, "FabriktOptions"))
            .addType(
                TypeSpec.interfaceBuilder(ClassName(PACKAGE, "FabriktOption"))
                    .addModifiers(KModifier.SEALED)
                    .addProperty("fabriktOption", nullableEnumType)
                    .build()
            )
            .addType(ExternalReferencesResolutionOption::class.asSpec())
            .addType(DateTimeOverrideOption::class.asSpec())
            .addType(ValidationLibraryOption::class.asSpec())
            .addType(ClientTargetOption::class.asSpec())
            .addType(ControllerTargetOption::class.asSpec())
            .build()
            .writeTo(outputDirectory.get().asFile)
    }

    internal companion object {

        internal const val PACKAGE = "ch.acanda.gradle.fabrikt"
        internal const val PROP_NAME = "name"
        internal const val PROP_OBJECTS = "objects"

        private val nullableEnumType = Enum::class.asTypeName().parameterizedBy(STAR).copy(nullable = true)

        internal fun fabriktGenerateExtension(
            className: ClassName,
            typeOverridesExtName: ClassName,
            clientExtName: ClassName,
            controllerExtName: ClassName,
            modelExtName: ClassName
        ) =
            TypeSpec.classBuilder(className)
                .addAnnotation(generated())
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
                .enabledValues()
                .fileProperty("apiFile")
                .filesProperty("apiFragments")
                .enumProperty("externalReferenceResolution", ExternalReferencesResolutionOption::class)
                .stringProperty("basePackage")
                .directoryProperty("outputDirectory")
                .stringProperty("sourcesPath")
                .stringProperty("resourcesPath")
                .enumProperty("validationLibrary", ValidationLibraryOption::class)
                .booleanProperty("quarkusReflectionConfig")
                .nestedProperty("typeOverrides", typeOverridesExtName)
                .nestedProperty("client", clientExtName)
                .nestedProperty("controller", controllerExtName)
                .nestedProperty("model", modelExtName)
                .build()

        internal fun fabriktExtension(className: ClassName, valueType: TypeName) = TypeSpec.classBuilder(className)
            .addAnnotation(generated())
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

        internal fun typeOverridesExtension(className: ClassName) = TypeSpec.classBuilder(className)
            .addAnnotation(generated())
            .addModifiers(KModifier.OPEN)
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
            .enumProperty("datetime", DateTimeOverrideOption::class)
            .build()

        internal fun clientExtension(className: ClassName) = TypeSpec.classBuilder(className)
            .addAnnotation(generated())
            .addModifiers(KModifier.OPEN)
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
            .booleanProperty("enabled")
            .booleanProperty("resilience4j")
            .booleanProperty("suspendModifier")
            .enumProperty("target", ClientTargetOption::class)
            .build()

        internal fun controllerExtension(className: ClassName) = TypeSpec.classBuilder(className)
            .addAnnotation(generated())
            .addModifiers(KModifier.OPEN)
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
            .booleanProperty("enabled")
            .booleanProperty("authentication")
            .booleanProperty("suspendModifier")
            .enumProperty("target", ControllerTargetOption::class)
            .build()

        internal fun modelExtension(className: ClassName) = TypeSpec.classBuilder(className)
            .addAnnotation(generated())
            .addModifiers(KModifier.OPEN)
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
            .booleanProperty("enabled")
            .booleanProperty("extensibleEnums")
            .booleanProperty("javaSerialization")
            .booleanProperty("quarkusReflection")
            .booleanProperty("micronautIntrospection")
            .booleanProperty("micronautReflection")
            .booleanProperty("includeCompanionObject")
            .booleanProperty("sealedInterfacesForOneOf")
            .booleanProperty("ignoreUnknownProperties")
            .build()

        internal fun generated() = AnnotationSpec.builder(Generated::class)
            .addMember("\"${ExtensionGenerator::class.qualifiedName}\"")
            .build()

        private fun <T> KClass<T>.asSpec(): TypeSpec where T : Enum<T>, T : FabriktOption {
            val constants = this.java.enumConstants
            val fabriktOptionType = constants
                .firstNotNullOf { it.fabriktOption }::class.asTypeName()
                .copy(nullable = constants.any { it.fabriktOption == null })
            val spec = TypeSpec.enumBuilder(this.simpleName.orEmpty())
                .addSuperinterface(ClassName(PACKAGE, "FabriktOption"))
                .primaryConstructor(
                    FunSpec.constructorBuilder()
                        .addParameter("fabriktOption", fabriktOptionType)
                        .build()
                )
                .addProperty(
                    PropertySpec
                        .builder("fabriktOption", fabriktOptionType, KModifier.PUBLIC, KModifier.OVERRIDE)
                        .initializer("fabriktOption")
                        .build()
                )
            constants.forEach {
                val enumSpec = TypeSpec.anonymousClassBuilder()
                val option = it.fabriktOption
                if (option != null) {
                    enumSpec.addSuperclassConstructorParameter(
                        "%T.%N",
                        fabriktOptionType.copy(nullable = false),
                        option.name
                    )
                } else {
                    enumSpec.addSuperclassConstructorParameter("null")
                }
                spec.addEnumConstant(it.name, enumSpec.build())
            }
            return spec.build()
        }
    }

}
