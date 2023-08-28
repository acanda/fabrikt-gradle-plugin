package ch.acanda.gradle.fabrikt.matchers

import io.kotest.matchers.shouldBe
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import java.io.File

internal infix fun <T> Property<T>.shouldContain(expected: T?): Property<T> {
    get() shouldBe expected
    return this
}

internal infix fun RegularFileProperty.shouldContain(expected: File?): RegularFileProperty {
    get().asFile shouldBe expected
    return this
}
