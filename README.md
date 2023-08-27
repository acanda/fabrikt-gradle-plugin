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
        apiFile.set(file("src/main/openapi/dog.yaml"))
        basePackage.set("com.example.api")
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
model classes from the OpenAPI specification in 'src/main/openapi/dog.yaml'. The
model classes will be generated in `build/generated/fabrikt/src/main/kotlin/`.

## Configuration

```kotlin
plugins {
    id("ch.acanda.gradle.fabrikt") version "<version>"
}

fabrikt {
    generate("dog") {
        // mandatory properties
        apiFile.set(file("src/main/openapi/dog.yaml"))
        basePackage.set("com.example.api")
        // optional properties with their default values
        outputDirectory.set(file("build/generated/fabrikt"))
    }
}
```
