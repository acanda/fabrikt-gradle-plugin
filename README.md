# Fabrikt Gradle Plugin

The Fabrikt Gradle Plugin integrates Fabrikt into your Gradle builds.
[Fabrikt](https://github.com/cjbooms/fabrikt) generates Kotlin data classes with
support for advanced features, Spring or Micronaut controllers, and OkHttp or
OpenFeign clients.

This plugin can be permanently integrated into your Gradle build and will ensure
contract and code always match, even as your APIs evolve in complexity.

## Usage

The plugin requires at least Gradle 8.3 and Java 17.

To use it with the Kotlin DSL (build.gradle.kts):

```kotlin
plugins {
    id("ch.acanda.gradle.fabrikt") version "<version>"
}

fabrikt {
    generate("dog") {
        apiFile("src/main/openapi/dog.yaml")
        basePackage("com.example.api")
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
}

fabrikt {
    generate("dog") {
        // mandatory properties
        apiFile("src/main/openapi/dog.yaml")
        basePackage("com.example.api")
        // optional properties with their default values
        apiFragments(emptySet())
        outputDirectory("build/generated/fabrikt")
        sourcesPath("src/main/kotlin")
        resourcesPath("src/main/resources")
        typeOverrides(null)
        validationLibrary(JAKARTA_VALIDATION)
        quarkusReflectionConfig(enabled)
        client {
            enabled(false)
            target(OK_HTTP)
            resilience4j(disabled)
            suspendModifier(disabled)
        }
        controller {
            enabled(false)
            options(emptySet())
            target(SPRING)
        }
        model {
            enabled(true)
            options(emptySet())
        }
    }
}
```

| Parameter               | Description                                                                                                                                          | Default value             | Supported types or values                                                                                                                                                                                                       |
|-------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| apiFile                 | The path to an Open API v3 specification, interpreted relative to the project directory.                                                             |                           | CharSequence, File, Path, RegularFile, or a Provider of any of those types.                                                                                                                                                     |
| apiFragments            | A set of paths to Open API v3 specification fragments, interpreted relative to the project directory.                                                | \<empty set>              | Varargs or Iterable of any type supported by [Project.files(...)](https://docs.gradle.org/current/kotlin-dsl/gradle/org.gradle.api/-project/files.html).                                                                        |
| basePackage             | The base package under which all code is built.                                                                                                      |                           | CharSequence, Provider\<CharSequence>.                                                                                                                                                                                          |
| outputDirectory         | The directory to which the generated classes are written, interpreted relative to the project directory.                                             | `build/generated/fabrikt` | CharSequence, File, Path, RegularFile, or a Provider of any of those types.                                                                                                                                                     |
| sourcesPath             | The path for generated source files, interpreted relative to the output directory.                                                                   | `src/main/kotlin`         | CharSequence, File, Path, RegularFile, or a Provider of any of those types.                                                                                                                                                     |
| resourcesPath           | The path for generated resource files, interpreted relative to the output directory.                                                                 | `src/main/resources`      | CharSequence, File, Path, RegularFile, or a Provider of any of those types.                                                                                                                                                     |
| typeOverrides           | Specifies non-default kotlin types for certain OAS types, e.g. generate `Instant` instead of `OffsetDateTime` for the OAS type `date`.               | not set                   | Enum: `DATETIME_AS_INSTANT`, `DATETIME_AS_INSTANT`.                                                                                                                                                                             |
| validationLibrary       | Specifies the validation library used for annotations in generated model classes.                                                                    | `JAKARTA_VALIDATION`      | Enum: `JAVAX_VALIDATION`, `JAKARTA_VALIDATION`.                                                                                                                                                                                 |
| quarkusReflectionConfig | Enableds generating the reflection-config.json file for quarkus integration projects.                                                                | `enabled`                 | Boolean: `enabled`, `disabled`, `true`, `false`.                                                                                                                                                                                |
| client.enabled          | Enables generating the http client code.                                                                                                             | `false`                   | Boolean: `true`, `false`.                                                                                                                                                                                                       |
| client.target           | The type of client you want to be generated.                                                                                                         | `OK_HTTP`                 | Enum: `OK_HTTP`, `OPEN_FEIGN`.                                                                                                                                                                                                  |
| client.resilience4j     | Generates a fault tolerance service for the client using the following library "io.github.resilience4j:resilience4j-all:+". Only for OkHttp clients. | `disabled`                | Boolean: `enabled`, `disabled`, `true`, `false`.                                                                                                                                                                                |
| client.suspendModifier  | Adds the suspend modifier to the generated client functions. Only for OpenFeign clients.                                                             | `disabled`                | Boolean: `enabled`, `disabled`, `true`, `false`.                                                                                                                                                                                |
| controller.enabled      | Enables generating the http controller code.                                                                                                         | `false`                   | Boolean: `true`, `false`.                                                                                                                                                                                                       |
| controller.options      | The options for the generated http controller code.                                                                                                  | \<empty set>              | Varargs, Iterable, or Provider\<Iterable> of Enum: `SUSPEND_MODIFIER`, `AUTHENTICATION`.                                                                                                                                        |
| controller.target       | The target framework tor the controllers you want to be generated.                                                                                   | `SPRING`                  | Enum: `SPRING`, `MICRONAUT`.                                                                                                                                                                                                    |
| model.enabled           | Enables generating the http model code.                                                                                                              | `true`                    | Boolean: `true`, `false`.                                                                                                                                                                                                       |
| model.options           | The options for the generated http model code.                                                                                                       | \<empty set>              | Varargs, Iterable, or Provider\<Iterable> of Enum: `X_EXTENSIBLE_ENUMS`, `JAVA_SERIALIZATION`, `QUARKUS_REFLECTION`, `MICRONAUT_INTROSPECTION`, `MICRONAUT_REFLECTION`, `INCLUDE_COMPANION_OBJECT`, `INCLUDE_COMPANION_OBJECT`. |
