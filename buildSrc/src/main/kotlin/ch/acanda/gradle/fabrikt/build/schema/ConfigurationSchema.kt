package ch.acanda.gradle.fabrikt.build.schema

/**
 * Maps the Gradle plugin option names (keys) to their respsctive option definitions (values).
 */
typealias OptionDefinitions = Map<String, OptionDefinition>

/**
 * Maps the Gradle plugin option names (keys) to their respective Fabrikt option names (values).
 * The values must match an enum constant name of a Fabrikt option enum class.
 */
typealias OptionMapping = Map<String, String?>

data class ConfigurationSchema(
    val options: OptionDefinitions
)

data class OptionDefinition(
    /** The fully qualified name of the Fabrikt option enum class. */
    val source: String,
    val mapping: OptionMapping
)
