package ch.acanda.gradle.fabrikt

import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.set
import io.kotest.property.arbitrary.stringPattern
import org.gradle.testfixtures.ProjectBuilder
import java.io.File
import java.nio.file.Paths

internal val generateTaskExtGen: Arb<GenerateTaskExtension> = arbitrary {
    val objects = ProjectBuilder.builder().build().objects
    objects.newInstance(GenerateTaskExtension::class.java, "api").apply {
        apiFile.set(pathGen.bind())
        apiFragments.setFrom(Arb.set(pathGen, 0..3).bind())
        externalReferenceResolution.set(Arb.enum<ExternalReferencesResolutionOption>().orNull(0.2).bind())
        basePackage.set(Arb.stringPattern("[a-z]{1,5}(\\.[a-z]{1,5}){0,3}").bind())
        outputDirectory.set(pathGen.bind())
        sourcesPath.set(Arb.stringPattern("[a-z]{1,5}(/[a-z]{1,5}){0,3}").orNull(0.2).bind())
        resourcesPath.set(Arb.stringPattern("[a-z]{1,5}(/[a-z]{1,5}){0,3}").orNull(0.2).bind())
        quarkusReflectionConfig.set(Arb.boolean().orNull(0.2).bind())
        typeOverrides.datetime.set(Arb.enum<DateTimeOverrideOption>().orNull(0.2).bind())
        validationLibrary.set(Arb.enum<ValidationLibraryOption>().orNull(0.2).bind())
        client.generate.set(Arb.boolean().orNull(0.2).bind())
        client.resilience4j.set(Arb.boolean().orNull(0.2).bind())
        client.suspendModifier.set(Arb.boolean().orNull(0.2).bind())
        client.target.set(Arb.enum<ClientTargetOption>().orNull(0.2).bind())
        controller.generate.set(Arb.boolean().orNull(0.2).bind())
        controller.authentication.set(Arb.boolean().orNull(0.2).bind())
        controller.suspendModifier.set(Arb.boolean().orNull(0.2).bind())
        controller.target.set(Arb.enum<ControllerTargetOption>().orNull(0.2).bind())
        model.generate.set(Arb.boolean().orNull(0.2).bind())
        model.extensibleEnums.set(Arb.boolean().orNull(0.2).bind())
        model.javaSerialization.set(Arb.boolean().orNull(0.2).bind())
        model.quarkusReflection.set(Arb.boolean().orNull(0.2).bind())
        model.micronautIntrospection.set(Arb.boolean().orNull(0.2).bind())
        model.micronautReflection.set(Arb.boolean().orNull(0.2).bind())
        model.includeCompanionObject.set(Arb.boolean().orNull(0.2).bind())
        model.nonNullMapValues.set(Arb.boolean().orNull(0.2).bind())
        model.sealedInterfacesForOneOf.set(Arb.boolean().orNull(0.2).bind())
        skip.set(Arb.boolean().orNull(0.2).bind())
    }
}

internal val generateTaskConfigGen: Arb<GenerateTaskConfiguration> = generateTaskExtGen.map { extConfig ->
    val project = ProjectBuilder.builder().build()
    val taskConfig = project.objects.newInstance(GenerateTaskConfiguration::class.java, "api")
    val defaults = project.objects.newInstance(GenerateTaskDefaults::class.java)
    initializeWithDefaults().invoke(taskConfig, extConfig, defaults)
    taskConfig
}

private val pathGen: Arb<File> = arbitrary {
    Paths.get(Arb.stringPattern("[A-Za-z0-9]{1,5}(/[A-Za-z0-9]{1,5}){0,3}").bind()).toFile()
}
