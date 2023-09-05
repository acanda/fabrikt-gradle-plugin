package ch.acanda.gradle.fabrikt.build.generator

import ch.acanda.gradle.fabrikt.build.ExtensionGenerator
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import org.gradle.api.Named

internal fun TypeSpec.Builder.named() = apply {
    addSuperinterface(Named::class)
    addProperty(
        PropertySpec.builder(ExtensionGenerator.PROP_NAME, String::class)
            .addModifiers(KModifier.PRIVATE)
            .initializer(ExtensionGenerator.PROP_NAME)
            .build()
    )
    addFunction(
        FunSpec.builder("getName")
            .addModifiers(KModifier.OVERRIDE)
            .returns(String::class)
            .addStatement("return %N", ExtensionGenerator.PROP_NAME)
            .build()
    )
}

