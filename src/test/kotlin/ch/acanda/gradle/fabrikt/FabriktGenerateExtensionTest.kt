package ch.acanda.gradle.fabrikt

import ch.acanda.gradle.fabrikt.matchers.shouldContain
import io.kotest.core.spec.style.WordSpec
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import java.io.File

class FabriktGenerateExtensionTest : WordSpec({

    "apiFile" should {

        "accept a file with an absolute path" {
            val (_, ext, spec) = createFixture()
            ext.apiFile(spec)
            ext.apiFile shouldContain spec
        }

        "accept a file with a path relative to the project directory" {
            val (project, ext, spec) = createFixture()
            ext.apiFile(spec.relativeTo(project.projectDir))
            ext.apiFile shouldContain spec
        }

        "accept a file provider with an absolute path" {
            val (project, ext, spec) = createFixture()
            val provider = project.objects.property(File::class.java).apply {
                set(spec)
            }
            ext.apiFile(provider)
            ext.apiFile shouldContain spec
        }

        "accept a file provider with a path relative to the project directory" {
            val (project, ext, spec) = createFixture()
            val provider = project.objects.property(File::class.java).apply {
                set(spec.relativeTo(project.projectDir))
            }
            ext.apiFile(provider)
            ext.apiFile shouldContain spec
        }

        "accept a string with an absolute path" {
            val (_, ext, spec) = createFixture()
            ext.apiFile(spec.toString())
            ext.apiFile shouldContain spec
        }

        "accept a string with a path relative to the project directory" {
            val (project, ext, spec) = createFixture()
            ext.apiFile(spec.relativeTo(project.projectDir).toString())
            ext.apiFile shouldContain spec
        }

        "accept a string provider with an absolute path" {
            val (project, ext, spec) = createFixture()
            val provider = project.objects.property(String::class.java).apply {
                set(spec.toString())
            }
            ext.apiFile(provider)
            ext.apiFile shouldContain spec
        }

        "accept a string provider with a path relative to the project directory" {
            val (project, ext, spec) = createFixture()
            val provider = project.objects.property(String::class.java).apply {
                set(spec.relativeTo(project.projectDir).toString())
            }
            ext.apiFile(provider)
            ext.apiFile shouldContain spec
        }

        "accept a RegularFile" {
            val (project, ext, spec) = createFixture()
            ext.apiFile(project.layout.projectDirectory.file(spec.relativeTo(project.projectDir).toString()))
            ext.apiFile shouldContain spec
        }

        "accept a RegularFile provider" {
            val (project, ext, spec) = createFixture()
            val provider = project.objects.fileProperty().apply {
                set(project.layout.projectDirectory.file(spec.relativeTo(project.projectDir).toString()))
            }
            ext.apiFile(provider)
            ext.apiFile shouldContain spec
        }

    }

}) {

    companion object {

        private fun createFixture(): Fixture {
            val project = ProjectBuilder.builder().build()
            val extension = FabriktGenerateExtension("api", project.objects)
            val spec = project.projectDir.resolve("spec.yaml")
            return Fixture(project, extension, spec)
        }

        private data class Fixture(
            val project: Project,
            val extension: FabriktGenerateExtension,
            val spec: File
        )

    }

}
