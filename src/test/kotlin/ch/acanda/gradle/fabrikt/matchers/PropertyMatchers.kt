package ch.acanda.gradle.fabrikt.matchers

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.file.FileSystemLocationProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import java.io.File

internal infix fun <T> Property<T>.shouldContain(expected: T?): Property<T> {
    get() shouldBe expected
    return this
}

internal infix fun <T> SetProperty<T>.shouldContainExactly(expected: T): SetProperty<T> {
    get() shouldContainExactly listOf(expected)
    return this
}

internal fun <T> SetProperty<T>.shouldBeEmpty(): SetProperty<T> {
    get().shouldBeEmpty()
    return this
}

internal infix fun <T : CharSequence> Property<T>.shouldContainString(expected: String?): Property<T> {
    get().toString() shouldBe expected
    return this
}

private typealias FslProperty<T> = FileSystemLocationProperty<T>

internal infix fun <T : FileSystemLocation> FslProperty<T>.shouldContain(expected: File?): FslProperty<T> {
    get().asFile shouldBe expected
    return this
}
