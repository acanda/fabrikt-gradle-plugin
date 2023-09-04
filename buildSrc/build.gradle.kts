plugins {
    kotlin("jvm")
    `java-gradle-plugin`
    id("io.gitlab.arturbosch.detekt") version "1.23.1"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.cjbooms:fabrikt:10.3.0")
    implementation("com.squareup:kotlinpoet:1.14.2")
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.1")
}

kotlin {
    jvmToolchain(17)
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom("${project.layout.projectDirectory}/../config/detekt.yaml")
}

testing {
    suites {
        @Suppress("unused")
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter("5.10.0")
            dependencies {
                val kotestVersion = "5.6.2"
                implementation("io.kotest:kotest-runner-junit5:$kotestVersion")
                implementation("io.kotest:kotest-assertions-core:$kotestVersion")
                implementation("io.kotest:kotest-property:$kotestVersion")
                runtimeOnly("org.junit.platform:junit-platform-launcher:1.10.0")
            }
        }
    }
}

