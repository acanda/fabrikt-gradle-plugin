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

enum class Dataflow { Input, Output }

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
    /**
     * Set to true if this property needs to be specified before the task can
     * be executed.
     */
    val mandatory: Boolean = false,
    /**
     * Specifies whether the property holds input or output data.
     */
    val dataflow: Dataflow = Dataflow.Input,
    /**
     * Set to false if this property should not be available in the extention
     * defaults.
     */
    val includeInDefaults: Boolean = true,
    /**
     * The default value that is set as the `convention` of the property.
     * If the property has the type `CharSequence`, the value will be put in
     * double quotes (`"`). For all other types, the value will be used as is
     * and can therefore be an expression. If the expressions starts with
     * `ProjectLayout`, then a `ProjectLayout` will be injected into the
     * respective defaults class and it will be replaced with the respective
     * class property in the expressions.
     */
    val default: String? = null,
)

data class OptionDefinition(
    /** The fully qualified name of the Fabrikt option enum class. */
    val source: String,
    val mapping: OptionMapping
)
