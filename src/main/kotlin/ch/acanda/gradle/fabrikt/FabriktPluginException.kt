package ch.acanda.gradle.fabrikt

open class FabriktPluginException(message: String, cause: Throwable?) : RuntimeException(message, cause) {
    constructor(message: String) : this(message, null)
}
