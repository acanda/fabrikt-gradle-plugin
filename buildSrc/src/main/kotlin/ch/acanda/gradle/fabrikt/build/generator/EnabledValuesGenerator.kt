package ch.acanda.gradle.fabrikt.build.generator

import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

/**
 * Adds the public values `enabled` and `disabled` to a type so they can be used
 * as values for configuration properties, e.g.:
 * ```kotlin
 *  controller {
 *    options {
 *      authentication(enabled)
 *      suspendModifier(disabled)
 *    }
 *  }
 * ```
 */
internal fun TypeSpec.Builder.enabledValues() = apply {
    addProperty(
        PropertySpec.builder("enabled", Boolean::class, KModifier.PUBLIC)
            .initializer("true")
            .build()
    )
    addProperty(
        PropertySpec.builder("disabled", Boolean::class, KModifier.PUBLIC)
            .initializer("false")
            .build()
    )
}
