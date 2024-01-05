# Fabrikt Gradle Plugin

The Fabrikt Gradle Plugin integrates Fabrikt into your Gradle builds.
[Fabrikt](https://github.com/cjbooms/fabrikt) generates Kotlin data classes with
support for advanced features, Spring or Micronaut controllers, and OkHttp or
OpenFeign clients.

This plugin can be permanently integrated into your Gradle build and will ensure
contract and code always match, even as your APIs evolve in complexity.

## Usage

The plugin requires at least Gradle 8.4 and a JRE 17.

To use it with the Kotlin DSL (build.gradle.kts):

```kotlin
plugins {
    id("ch.acanda.gradle.fabrikt") version "<version>"
}

fabrikt {
    generate("dog") {
        apiFile = file("src/main/openapi/dog.yaml")
        basePackage = "com.example.api"
    }
}
```

To use it with the Groovy DSL (build.gradle):

```groovy
plugins {
    id 'ch.acanda.gradle.fabrikt' version '<version>'
}

fabrikt {
    dog {
        apiFile = file('src/main/openapi/dog.yaml')
        basePackage = 'com.example.api'
    }
}
```

When running `gradle fabriktGenerate`, the examples above will generate the
model classes from the OpenAPI specification in `src/main/openapi/dog.yaml`. The
model classes will be generated in `build/generated/fabrikt/src/main/kotlin/`.

## Configuration

```kotlin
plugins {
    id("ch.acanda.gradle.fabrikt") version "<version>"
    // If you are using IntelliJ IDEA, the plugin will automatically add the
    // output directory as a generated sources directory.
    idea
}

fabrikt {
    generate("dog") {
        // mandatory properties
        apiFile = file("src/main/openapi/dog.yaml")
        basePackage = "com.example.api"
        // optional properties with their default values
        apiFragments = files()
        outputDirectory = file("build/generated/sources/fabrikt")
        sourcesPath = "src/main/kotlin"
        resourcesPath = "src/main/resources"
        validationLibrary = Javax
        quarkusReflectionConfig = enabled
        typeOverrides {
            datetime = OffsetDateTime
        }
        client {
            enabled = false
            target = OK_HTTP
            resilience4j = disabled
            suspendModifier = disabled
        }
        controller {
            enabled = false
            authentication = disabled
            suspendModifier = disabled
            target = SPRING
        }
        model {
            enabled = true
            extensibleEnums = disabled
            javaSerialization = disabled
            quarkusReflection = disabled
            micronautIntrospection = disabled
            micronautReflection = disabled
            includeCompanionObject = disabled
            sealedInterfacesForOneOf = disabled
            ignoreUnknownProperties = disabled
        }
    }
}
```

| Property                       | Description                                                                                                                                                                                              | Default value                     |
|--------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------|
| apiFile                        | The path to an Open API v3 specification, interpreted relative to the project directory.                                                                                                                 |                                   |
| apiFragments                   | A set of paths to Open API v3 specification fragments, interpreted relative to the project directory.                                                                                                    | `files()`                         |
| basePackage                    | The base package under which all code is built.                                                                                                                                                          |                                   |
| outputDirectory                | The directory to which the generated classes are written, interpreted relative to the project directory.                                                                                                 | `build/generated/sources/fabrikt` |
| sourcesPath                    | The path for generated source files, interpreted relative to the output directory.                                                                                                                       | `src/main/kotlin`                 |
| resourcesPath                  | The path for generated resource files, interpreted relative to the output directory.                                                                                                                     | `src/main/resources`              |
| typeOverrides.datetime         | Specifies the Kotlin type for the OAS type `datetime`.<br/>Values: `OffsetDateTime`, `Instant`, `LocalDateTime`.                                                                                         | `OffsetDateTime`                  |
| validationLibrary              | Specifies the validation library used for annotations in generated model classes.<br/>Values: `Javax`, `Jakarta`.                                                                                        | `Jakarta`                         |
| quarkusReflectionConfig        | Enableds generating the reflection-config.json file for quarkus integration projects.<br/>Values: `enabled`, `disabled`, `true`, `false`.                                                                | `enabled`                         |
| client.enabled                 | Enables generating the http client code.<br/>Values: `true`, `false`.                                                                                                                                    | `false`                           |
| client.target                  | The type of client you want to be generated.<br/>`OK_HTTP`, `OPEN_FEIGN`.                                                                                                                                | `OK_HTTP`                         |
| client.resilience4j            | Generates a fault tolerance service for the client using the following library "io.github.resilience4j:resilience4j-all:+". Only for OkHttp clients.<br/>Values: `enabled`, `disabled`, `true`, `false`. | `disabled`                        |
| client.suspendModifier         | Enables adding the suspend modifier to the generated client functions. Only for OpenFeign clients.<br/>Values: `enabled`, `disabled`, `true`, `false`.                                                   | `disabled`                        |
| controller.enabled             | Enables generating the http controller code.<br/>Values: `true`, `false`.                                                                                                                                | `false`                           |
| controller.authentication      | Enables adding the authentication parameter to the generated controller functions.<br/>Values: `enabled`, `disabled`, `true`, `false`.                                                                   | `disabled`                        |
| controller.suspendModifier     | Enables adding the suspend modifier to the generated controller functions.<br/>Values: `enabled`, `disabled`, `true`, `false`.                                                                           | `disabled`                        |
| controller.target              | The target framework tor the controllers you want to be generated.<br/>Values: `SPRING`, `MICRONAUT`.                                                                                                    | `SPRING`                          |
| model.enabled                  | Enables generating the http model code.<br/>Values: `true`, `false`.                                                                                                                                     | `true`                            |
| model.extensibleEnums          | Enables treating x-extensible-enums as enums.<br/>Values: `enabled`, `disabled`, `true`, `false`.                                                                                                        | `disabled`                        |
| model.javaSerialization        | Enables adding the Java `Serializable` interface to the generated models.<br/>Values: `enabled`, `disabled`, `true`, `false`.                                                                            | `disabled`                        |      
| model.quarkusReflection        | Enables adding `@RegisterForReflection` to the generated models.<br/>Requires the dependency `io.quarkus:quarkus-core:+`.<br/>Values: `enabled`, `disabled`, `true`, `false`.                            | `disabled`                        |
| model.micronautIntrospection   | Enables adding `@Introspected` to the generated models.<br/>Requires the dependency `io.micronaut:micronaut-core:+`.<br/>Values: `enabled`, `disabled`, `true`, `false`.                                 | `disabled`                        |
| model.micronautReflection      | Enables adding `@ReflectiveAccess` to the generated models.<br/>Requires the dependency `io.micronaut:micronaut-core:+`.<br/>Values: `enabled`, `disabled`, `true`, `false`.                             | `disabled`                        |      
| model.includeCompanionObject   | Enables adding a companion object to the generated models.<br/>Values: `enabled`, `disabled`, `true`, `false`.                                                                                           | `disabled`                        |
| model.sealedInterfacesForOneOf | Enables the generation of interfaces for discriminated `oneOf` types.<br/>Values: `enabled`, `disabled`, `true`, `false`.                                                                                | `disabled`                        |
| model.ignoreUnknownProperties  | Enables adding `@JacksonIgnoreProperties(ignoreUnknown = true)` to the generated models.<br/>Values: `enabled`, `disabled`, `true`, `false`.                                                             | `disabled`                        |

## Development

### Local Installation

You can use `gradle publishToMavenLocal` to install the plugin to your local Maven repository.
Then add the local maven repository to settings.gradle.kts of the project where you want to use the plugin:

```kotlin
pluginManagement {
    repositories {
        mavenLocal()
        // other repositories go here
    }
}
```
