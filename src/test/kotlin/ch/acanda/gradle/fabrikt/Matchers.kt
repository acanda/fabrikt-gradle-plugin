package ch.acanda.gradle.fabrikt

import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.typeOf

internal infix fun GenerateTaskConfiguration.shouldMatch(expected: GenerateTaskConfiguration) {
    this.name shouldBe expected.name
    this propertiesShouldBe expected
}

private fun <T : Any> T?.shouldNotBeNull(): T {
    withClue(this) {
        this shouldNotBe null
    }
    return this!!
}

private infix fun <T : Any> T.propertiesShouldBe(expected: T) {
    @Suppress("UNCHECKED_CAST")
    val kClass = expected::class as KClass<T>
    kClass.declaredMemberProperties.forEach { prop ->
        if (prop.isGradleProperty()) {
            val actual = (prop.get(this) as Property<*>).get()
            val expectedValue = (prop.get(expected) as Property<*>).get()
            withClue("Expected \"${prop.name}\" to be <$expectedValue> but was <$actual>.") {
                expectedValue shouldBe actual
            }
        } else if (prop.isNestedConfiguration()) {
            val actualNested = prop.get(this).shouldNotBeNull()
            val expectedNested = prop.get(expected).shouldNotBeNull()
            withClue("Nested property \"${prop.name}\"") {
                actualNested propertiesShouldBe expectedNested
            }
        }
    }

}

private fun KProperty<*>.isGradleProperty() = returnType.isSubtypeOf(typeOf<Property<*>>())

private fun KProperty<*>.isNestedConfiguration() = getter.annotations.any { it is Nested }
