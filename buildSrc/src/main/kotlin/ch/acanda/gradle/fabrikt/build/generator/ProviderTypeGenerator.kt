package ch.acanda.gradle.fabrikt.build.generator

import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.WildcardTypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import org.gradle.api.provider.Provider
import java.io.File

internal inline fun <reified T> provider() = when {
    T::class.java.isEnum -> Provider::class.asClassName().parameterizedBy(T::class.asTypeName())
    else -> provider(T::class.asTypeName())
}

internal fun provider(valueType: TypeName) =
    Provider::class.asClassName().parameterizedBy(
        when (valueType) {
            File::class.asTypeName() -> valueType
            else -> WildcardTypeName.producerOf(valueType)
        }
    )
