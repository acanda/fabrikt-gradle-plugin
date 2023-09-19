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

The plugin offers the same parameters as the Fabrikt CLI, which are documented
in the [Fabrikt Uasage Instructions](https://github.com/cjbooms/fabrikt#usage-instructions).

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
        outputDirectory("build/generated/fabrikt")
        apiFragments(emptySet())
        targets(HTTP_MODELS)
        client {
            enabled(false)
            options(emptySet())
            target(OK_HTTP)
        }
        controller {
            enabled(false)
            options(emptySet())
            target(SPRING)
        }
    }
}
```

| Parameter          | Description                                                                                              | Default value             | Supported types or values                                                                                                                                |
|--------------------|----------------------------------------------------------------------------------------------------------|---------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------|
| apiFile            | The path to an Open API v3 specification, interpreted relative to the project directory.                 |                           | CharSequence, File, Path, RegularFile, or a Provider of any of those types.                                                                              |
| basePackage        | The base package under which all code is built.                                                          |                           | CharSequence, Provider\<CharSequence>.                                                                                                                   |
| outputDirectory    | The directory to which the generated classes are written, interpreted relative to the project directory. | `build/generated/fabrikt` | CharSequence, File, Path, RegularFile, or a Provider of any of those types.                                                                              |
| apiFragments       | A set of paths to Open API v3 specification fragments, interpreted relative to the project directory.    | \<empty set>              | Varargs or Iterable of any type supported by [Project.files(...)](https://docs.gradle.org/current/kotlin-dsl/gradle/org.gradle.api/-project/files.html). |
| targets            | Targets are the parts of the application that you want to be generated.                                  | `HTTP_MODELS`             | Varargs, Iterable, or Provider\<Iterable> of Enum: `HTTP_MODELS`, `CONTROLLERS`, `CLIENT`, `QUARKUS_REFLECTION_CONFIG`.                                  |
| client.enabled     | Enables generating the http client code.                                                                 | `false`                   | Boolean: `true`, `false`.                                                                                                                                |
| client.options     | The options for the generated http client code.                                                          | \<empty set>              | Varargs, Iterable, or Provider\<Iterable> of Enum: `RESILIENCE4J`, `SUSPEND_MODIFIER`.                                                                   |
| client.target      | The type of client you want to be generated.                                                             | `OK_HTTP`                 | Enum: `OK_HTTP`, `OPEN_FEIGN`.                                                                                                                           |
| controller.enabled | Enables generating the http controller code.                                                             | `false`                   | Boolean: `true`, `false`.                                                                                                                                |
| controller.options | The options for the generated http controller code.                                                      | \<empty set>              | Varargs, Iterable, or Provider\<Iterable> of Enum: `SUSPEND_MODIFIER`, `AUTHENTICATION`.                                                                 |
| controller.target  | The target framework tor the controllers you want to be generated.                                       | `SPRING`                  | Enum: `SPRING`, `MICRONAUT`.                                                                                                                             |
