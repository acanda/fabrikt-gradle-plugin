package ch.acanda.gradle.fabrikt

import java.io.File

internal fun File.listFilesRelative() =
    walkTopDown()
        .filter { it.isFile }
        .map { it.toRelativeString(this).replace('\\', '/') }
        .sorted()
        .toList()

internal fun String.packageToPath() =
    replace('.', '/')
