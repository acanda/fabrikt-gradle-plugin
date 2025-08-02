# Fabrikt Gradle Plugin

The Fabrikt Gradle Plugin integrates Fabrikt into your Gradle builds.
[Fabrikt](https://github.com/cjbooms/fabrikt) generates Kotlin data classes with
support for advanced features, Spring or Micronaut controllers, Ktor route handlers, and OkHttp or
OpenFeign clients.

This plugin can be permanently integrated into your Gradle build and will ensure
contract and code always match, even as your APIs evolve in complexity.

## Usage

[![Gradle Plugin Portal Version](https://img.shields.io/gradle-plugin-portal/v/ch.acanda.gradle.fabrikt?style=flat)](https://plugins.gradle.org/plugin/ch.acanda.gradle.fabrikt)

The plugin requires at least Gradle 9.0 and a JRE 21.
If you are using Gradle 8.x, you can use version 1.19.0 of the plugin.

To use it with the Kotlin DSL (build.gradle.kts):

```kotlin
plugins {
    id("ch.acanda.gradle.fabrikt") version "1.19.0"
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
    id 'ch.acanda.gradle.fabrikt' version '1.19.0'
}

fabrikt {
    dog {
        apiFile = file('src/main/openapi/dog.yaml')
        basePackage = 'com.example.api'
    }
}
```
> Note: `defaults` are currently not supported in the Groovy DSL. If you want to
> use defaults, you have to use the Kotlin DSL.
> 
> Also, some configuration options are reserved words or type names that are 
> imported by Groovy by default, so you have to prefix them with `owner.`.
> ```groovy
> typeOverrides {
>   binary = owner.InputStream
>   owner.'byte' = owner.String
>   datetime = owner.Instant
> }
> ```

When running `gradle fabriktGenerate`, the examples above will generate the
model classes from the OpenAPI specification in `src/main/openapi/dog.yaml`. The
model classes will be generated in
`build/generated/sources/fabrikt/src/main/kotlin/`.

The plugin provides the following tasks:

- `fabriktGenerate`: generates code for all configurations, unless they have
  `skip` set to  `true`.
- `fabriktGenerate[Name]`: generates code for the configuration with the
  specified name. The suffix `[Name]` is derived from the configuration´s name
  by removing non-alphanumeric characters and converting the rest to CamelCase.
  E.g. the code for the configuration `generate("dog-api") { ... }` can be generated
  with `gradle fabriktGenerateDogApi`.

## Configuration

```kotlin
plugins {
    id("ch.acanda.gradle.fabrikt") version "1.19.0"
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
        externalReferenceResolution = targeted
        outputDirectory = file("build/generated/sources/fabrikt")
        sourcesPath = "src/main/kotlin"
        resourcesPath = "src/main/resources"
        validationLibrary = Javax
        quarkusReflectionConfig = enabled
        typeOverrides {
            datetime = OffsetDateTime
            byte = ByteArray
            binary = ByteArray
            uri = URI
            uuid = UUID
            date = LocalDate
        }
        client {
            generate = disabled
            target = OkHttp
            resilience4j = disabled
            suspendModifier = disabled
            springResponseEntityWrapper = disabled
            springCloudOpenFeignStarterAnnotation = disabled
            openFeignClientName = "fabrikt-client"
        }
        controller {
            generate = disabled
            authentication = disabled
            suspendModifier = disabled
            completionStage = disabled
            target = Spring
        }
        model {
            generate = enabled
            extensibleEnums = disabled
            javaSerialization = disabled
            quarkusReflection = disabled
            micronautIntrospection = disabled
            micronautReflection = disabled
            includeCompanionObject = disabled
            sealedInterfacesForOneOf = disabled
            ignoreUnknownProperties = disabled
            serializationLibrary = Jackson
        }
        skip = false
    }
}
```

| Property                                     | Description                                                                                                                                                                                                                                                                                                                        | Default value                     |
|----------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------|
| apiFile                                      | The path to an Open API v3 specification, interpreted relative to the project directory.                                                                                                                                                                                                                                           |                                   |
| apiFragments                                 | A set of paths to Open API v3 specification fragments, interpreted relative to the project directory.                                                                                                                                                                                                                              | `files()`                         |
| externalReferenceResolution                  | Specify to which degree referenced schemas from external files are included in model generation. `targeted` generates models only for directly referenced schemas in external API files. `aggressive` triggers generation of every external schema in a file containing a referenced schema.<br/>Values: `targeted`, `aggressive`. | `targeted`                        |
| basePackage                                  | The base package under which all code is built.                                                                                                                                                                                                                                                                                    |                                   |
| outputDirectory                              | The directory to which the generated classes are written, interpreted relative to the project directory.                                                                                                                                                                                                                           | `build/generated/sources/fabrikt` |
| sourcesPath                                  | The path for generated source files, interpreted relative to the output directory.                                                                                                                                                                                                                                                 | `src/main/kotlin`                 |
| resourcesPath                                | The path for generated resource files, interpreted relative to the output directory.                                                                                                                                                                                                                                               | `src/main/resources`              |
| typeOverrides.datetime                       | Specifies the Kotlin type for the OAS type `datetime`.<br/>Values: `OffsetDateTime`, `Instant`, `LocalDateTime`.                                                                                                                                                                                                                   | `OffsetDateTime`                  |
| typeOverrides.byte                           | Specifies the Kotlin type for the OAS type `string` with format `byte`.<br/>Values: `ByteArray`, `String`.                                                                                                                                                                                                                         | `ByteArray`                       |
| typeOverrides.binary                         | Specifies the Kotlin type for the OAS type `string` with format `binary`.<br/>Values: `ByteArray`, `InputStream`, `String`.                                                                                                                                                                                                        | `ByteArray`                       |
| typeOverrides.uri                            | Specifies the Kotlin type for the OAS type `string` with format `uri`.<br/>Values: `URI`, `String`.                                                                                                                                                                                                                                | `URI`                             |
| typeOverrides.uuid                           | Specifies the Kotlin type for the OAS type `string` with format `uuid`.<br/>Values: `UUID`, `String`.                                                                                                                                                                                                                              | `UUID`                            |
| typeOverrides.date                           | Specifies the Kotlin type for the OAS type `string` with format `date`.<br/>Values: `LocalDate`, `String`.                                                                                                                                                                                                                         | `LocalDate`                       |
| validationLibrary                            | Specifies the validation library used for annotations in generated model classes.<br/>Values: `Javax`, `Jakarta`, `NoValidation`.                                                                                                                                                                                                  | `Jakarta`                         |
| quarkusReflectionConfig                      | Enables generating the reflection-config.json file for quarkus integration projects.<br/>Values: `enabled`, `disabled`, `true`, `false`.                                                                                                                                                                                           | `enabled`                         |
| client.generate                              | Enables generating the http client code.<br/>Values: `enabled`, `disabled`, `true`, `false`.                                                                                                                                                                                                                                       | `disabled`                        |
| client.target                                | The type of client you want to be generated.<br/>`OkHttp`, `OpenFeign`.                                                                                                                                                                                                                                                            | `OkHttp`                          |
| client.resilience4j                          | Generates a fault tolerance service for the client using the following library "io.github.resilience4j:resilience4j-all:+". Only for OkHttp clients.<br/>Values: `enabled`, `disabled`, `true`, `false`.                                                                                                                           | `disabled`                        |
| client.suspendModifier                       | Enables adding the suspend modifier to the generated client functions. Only for OpenFeign clients.<br/>Values: `enabled`, `disabled`, `true`, `false`.                                                                                                                                                                             | `disabled`                        |
| client.springResponseEntityWrapper           | Enables adding the Spring-ResponseEntity generic around the response to be able to get response headers and status (only for target `OpenFeign`).<br/>Values: `enabled`, `disabled`, `true`, `false`.                                                                                                                              | `disabled`                        |
| client.springCloudOpenFeignStarterAnnotation | Enables adding the the `@FeignClient` annotation to generated client interface (only for target `OpenFeign`).<br/>Values: `enabled`, `disabled`, `true`, `false`.                                                                                                                                                                  | `disabled`                        |
| client.openFeignClientName                   | Specifies the OpenFeign client name for `spring-cloud-starter-openfeign` (only for target `OpenFeign`).                                                                                                                                                                                                                            | `fabrikt-client`                  |
| controller.generate                          | Enables generating the http controller code.<br/>Values: `enabled`, `disabled`, `true`, `false`.                                                                                                                                                                                                                                   | `disabled`                        |
| controller.authentication                    | Enables adding the authentication parameter to the generated controller functions.<br/>Values: `enabled`, `disabled`, `true`, `false`.                                                                                                                                                                                             | `disabled`                        |
| controller.suspendModifier                   | Enables adding the suspend modifier to the generated controller functions.<br/>Values: `enabled`, `disabled`, `true`, `false`.                                                                                                                                                                                                     | `disabled`                        |
| controller.completionStage                   | Enables the generated controller functions to have type CompletionStage (only for target `Spring`).<br/>Values: `enabled`, `disabled`, `true`, `false`.                                                                                                                                                                            | `disabled`                        |
| controller.target                            | The target framework tor the controllers you want to be generated.<br/>Values: `Spring`, `Micronaut`, `Ktor`.                                                                                                                                                                                                                      | `Spring`                          |
| model.generate                               | Enables generating the http model code.<br/>Values: `enabled`, `disabled`, `true`, `false`.                                                                                                                                                                                                                                        | `enabled`                         |
| model.extensibleEnums                        | Enables treating x-extensible-enums as enums.<br/>Values: `enabled`, `disabled`, `true`, `false`.                                                                                                                                                                                                                                  | `disabled`                        |
| model.javaSerialization                      | Enables adding the Java `Serializable` interface to the generated models.<br/>Values: `enabled`, `disabled`, `true`, `false`.                                                                                                                                                                                                      | `disabled`                        |
| model.quarkusReflection                      | Enables adding `@RegisterForReflection` to the generated models.<br/>Requires the dependency `io.quarkus:quarkus-core:+`.<br/>Values: `enabled`, `disabled`, `true`, `false`.                                                                                                                                                      | `disabled`                        |
| model.micronautIntrospection                 | Enables adding `@Introspected` to the generated models.<br/>Requires the dependency `io.micronaut:micronaut-core:+`.<br/>Values: `enabled`, `disabled`, `true`, `false`.                                                                                                                                                           | `disabled`                        |
| model.micronautReflection                    | Enables adding `@ReflectiveAccess` to the generated models.<br/>Requires the dependency `io.micronaut:micronaut-core:+`.<br/>Values: `enabled`, `disabled`, `true`, `false`.                                                                                                                                                       | `disabled`                        |
| model.includeCompanionObject                 | Enables adding a companion object to the generated models.<br/>Values: `enabled`, `disabled`, `true`, `false`.                                                                                                                                                                                                                     | `disabled`                        |
| model.sealedInterfacesForOneOf               | Enables the generation of interfaces for discriminated `oneOf` types.<br/>Values: `enabled`, `disabled`, `true`, `false`.                                                                                                                                                                                                          | `disabled`                        |
| model.nonNullMapValues                       | This option makes map values non-null when enabled. The default is to make map values nullable.<br/>Values: `enabled`, `disabled`, `true`, `false`.                                                                                                                                                                                | `disabled`                        |
| model.ignoreUnknownProperties                | Enables adding `@JacksonIgnoreProperties(ignoreUnknown = true)` to the generated models.<br/>Values: `enabled`, `disabled`, `true`, `false`.                                                                                                                                                                                       | `disabled`                        |
| model.suffix                                 | Specifies a custom suffix for all generated model classes.                                                                                                                                                                                                                                                                         | `null` (no suffix)                |
| model.serializationLibrary                   | Specifies the serialization library to use for annotations in generated model classes.<br/>Values: `Jackson`, `Kotlin`.                                                                                                                                                                                                            | `Jackson`                         |
| skip                                         | Skips generating code if set to `true` when running the task `fabriktGenerate`. Tasks generating code for a single configuration, i.e. `fabriktGenerate[Name]`, ignore this setting.<br/>Values: `true`, `false`.                                                                                                                  | `false`                           |

### Defaults

If you have many OpenAPI specifications with nearly the same configuration, you
can set the common values in the `defaults`. All properties except `apiFile` and
`basePackage` can be configured with default values.

> Note: The `defaults` block is only supported in the Kotlin DSL and not in the 
> Groovy DSL.

```kotlin
fabrikt {
    defaults {
        client {
            generate = true
            target = OpenFeign
        }
    }
    generate("dog") {
        apiFile = file("src/main/openapi/dog.yaml")
        basePackage = "com.example.dog"
    }
    generate("cat") {
        apiFile = file("src/main/openapi/cat.yaml")
        basePackage = "com.example.cat"
    }
}
```

The above example generates OpenFeign clients for both the dog and cat
specifications.

## Development

### Local Installation

You can use `gradle publishToMavenLocal` to install the plugin to your local
Maven repository. Then add the local maven repository to settings.gradle.kts of
the project where you want to use the plugin:

```kotlin
pluginManagement {
    repositories {
        mavenLocal()
        // other repositories go here
    }
}
```

### Publish to Gradle Plugin Portal

Validate the plugin:

```bash
gradle publishPlugins --validate-only
```

Publish the plugin:

```bash
gradle publishPlugins \
  -Pgradle.publish.key=... \ 
  -Pgradle.publish.secret=... \
  -Psigning.gnupg.keyName=...
```
