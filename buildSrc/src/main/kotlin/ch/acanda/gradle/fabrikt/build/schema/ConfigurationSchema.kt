package ch.acanda.gradle.fabrikt.build.schema

/**
 * Maps the Gradle plugin configuration names (keys) to their respsctive
 * configuration definitions (values).
 */
typealias ConfigurationDefinitions = Map<String, ConfigurationDefinition>

/**
 * Maps the Gradle plugin property names (keys) to their respsctive
 * property definitions (values).
 */
typealias PropertiesDefinitions = Map<String, PropertyDefinition>

/**
 * Maps the Gradle plugin option names (keys) to their respsctive option
 * definitions (values).
 */
typealias OptionDefinitions = Map<String, OptionDefinition>

/**
 * Maps the Gradle plugin option names (keys) to their respective Fabrikt option
 * names (values). The values must match an enum constant name of a Fabrikt
 * option enum class.
 */
typealias OptionMapping = Map<String, String?>

data class ConfigurationSchema(
    val configurations: ConfigurationDefinitions,
    val options: OptionDefinitions,
)

data class ConfigurationDefinition(
    /**
     * Set to `true` if the configuration implements the interface
     * [org.gradle.api.Named].
     */
    val named: Boolean = false,
    val properties: PropertiesDefinitions,
)

data class PropertyDefinition(
    /**
     * The type of the property can be any of the
     * [predefined types][ch.acanda.gradle.fabrikt.build.builder.getClassName]
     * or the name of one of the options or configurations.
     */
    val type: String,
)

data class OptionDefinition(
    /** The fully qualified name of the Fabrikt option enum class. */
    val source: String,
    val mapping: OptionMapping
)
