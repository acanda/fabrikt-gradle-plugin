package ch.acanda.gradle.fabrikt.matchers

import io.kotest.matchers.shouldBe
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.file.FileSystemLocationProperty
import org.gradle.api.provider.Property
import java.io.File

internal infix fun <T : Any> Property<T>.shouldContain(expected: T?): Property<T> {
    getOrNull() shouldBe expected
    return this
}

internal infix fun <T : CharSequence> Property<T>.shouldContainString(expected: String?): Property<T> {
    getOrNull()?.toString() shouldBe expected
    return this
}

private typealias FslProperty<T> = FileSystemLocationProperty<T>

internal infix fun <T : FileSystemLocation> FslProperty<T>.shouldContain(expected: File?): FslProperty<T> {
    getOrNull()?.asFile shouldBe expected
    return this
}
