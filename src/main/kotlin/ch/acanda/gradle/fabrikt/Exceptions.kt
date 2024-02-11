package ch.acanda.gradle.fabrikt

import java.nio.file.Path
import kotlin.io.path.absolutePathString

sealed class FabriktPluginException(message: String, cause: Throwable?) : RuntimeException(message, cause)

class GeneratorException(val apiFile: Path, cause: Throwable?) :
    FabriktPluginException("Failed to generate code for OpenApi specification ${apiFile.absolutePathString()}.", cause)
