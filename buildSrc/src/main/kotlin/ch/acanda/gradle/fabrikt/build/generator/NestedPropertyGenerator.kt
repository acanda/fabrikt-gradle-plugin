package ch.acanda.gradle.fabrikt.build.generator

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.gradle.api.Action
import org.gradle.api.tasks.Nested

internal fun TypeSpec.Builder.nestedProperty(name: String, typeName: TypeName) = apply {
    addProperty(
        PropertySpec.builder(name, typeName, KModifier.PUBLIC)
            .addAnnotation(Nested::class)
            .initializer("objects.newInstance(%T::class.java)", typeName)
            .build()
    )
    addFunction(
        FunSpec.builder(name)
            .addParameter("action", Action::class.asClassName().parameterizedBy(typeName))
            .addStatement("action.execute(%N)", name)
            .build()
    )
}
