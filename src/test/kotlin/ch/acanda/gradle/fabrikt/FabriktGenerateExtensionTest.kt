package ch.acanda.gradle.fabrikt

import ch.acanda.gradle.fabrikt.matchers.shouldContain
import ch.acanda.gradle.fabrikt.matchers.shouldContainString
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

    "basePackage" should {

        "accept a string" {
            val (_, ext, _) = createFixture()
            ext.basePackage(BASE_PACKAGE)
            ext.basePackage shouldContain BASE_PACKAGE
        }

        "accept a string provider" {
            val (project, ext, _) = createFixture()
            val provider = project.objects.property(String::class.java).apply {
                set(BASE_PACKAGE)
            }
            ext.basePackage(provider)
            ext.basePackage shouldContain BASE_PACKAGE
        }

        "accept a CharSequence" {
            val (_, ext, _) = createFixture()
            val basePackage: CharSequence = StringBuilder(BASE_PACKAGE)
            ext.basePackage(basePackage)
            ext.basePackage shouldContainString BASE_PACKAGE
        }

        "accept a CharSequence provider" {
            val (project, ext, _) = createFixture()
            val provider = project.objects.property(CharSequence::class.java).apply {
                val basePackage: CharSequence = StringBuilder(BASE_PACKAGE)
                set(basePackage)
            }
            ext.basePackage(provider)
            ext.basePackage shouldContainString BASE_PACKAGE
        }

    }

    "outputDirectory" should {

        fun outDir(project: Project) = project.layout.buildDirectory

        "accept a file with an absolute path" {
            val (project, ext, _) = createFixture()
            val outputDirectory = outDir(project).asFile.get().absoluteFile
            ext.outputDirectory(outputDirectory)
            ext.outputDirectory shouldContain outputDirectory
        }

        "accept a file with a path relative to the project directory" {
            val (project, ext, _) = createFixture()
            val outputDirectory = outDir(project).asFile.get().absoluteFile
            ext.outputDirectory(outputDirectory.relativeTo(project.projectDir))
            ext.outputDirectory shouldContain outputDirectory
        }

        "accept a file provider with an absolute path" {
            val (project, ext, _) = createFixture()
            val outputDirectory = outDir(project).asFile.get().absoluteFile
            val provider = project.objects.property(File::class.java).apply {
                set(outputDirectory)
            }
            ext.outputDirectory(provider)
            ext.outputDirectory shouldContain outputDirectory
        }

        "accept a file provider with a path relative to the project directory" {
            val (project, ext, _) = createFixture()
            val outputDirectory = outDir(project).asFile.get().absoluteFile
            val provider = project.objects.property(File::class.java).apply {
                set(outputDirectory.relativeTo(project.projectDir))
            }
            ext.outputDirectory(provider)
            ext.outputDirectory shouldContain outputDirectory
        }

        "accept a string with an absolute path" {
            val (project, ext, _) = createFixture()
            val outputDirectory = outDir(project).asFile.get().absoluteFile
            ext.outputDirectory(outputDirectory.toString())
            ext.outputDirectory shouldContain outputDirectory
        }

        "accept a string with a path relative to the project directory" {
            val (project, ext, _) = createFixture()
            val outputDirectory = outDir(project).asFile.get().absoluteFile
            ext.outputDirectory(outputDirectory.relativeTo(project.projectDir).toString())
            ext.outputDirectory shouldContain outputDirectory
        }

        "accept a string provider with an absolute path" {
            val (project, ext, _) = createFixture()
            val outputDirectory = outDir(project).asFile.get().absoluteFile
            val provider = project.objects.property(String::class.java).apply {
                set(outputDirectory.toString())
            }
            ext.outputDirectory(provider)
            ext.outputDirectory shouldContain outputDirectory
        }

        "accept a string provider with a path relative to the project directory" {
            val (project, ext, _) = createFixture()
            val outputDirectory = outDir(project).asFile.get().absoluteFile
            val provider = project.objects.property(String::class.java).apply {
                set(outputDirectory.relativeTo(project.projectDir).toString())
            }
            ext.outputDirectory(provider)
            ext.outputDirectory shouldContain outputDirectory
        }

        "accept a directory" {
            val (project, ext, _) = createFixture()
            val outputDirectory = outDir(project).get()
            ext.outputDirectory(outputDirectory)
            ext.outputDirectory shouldContain outputDirectory.asFile.absoluteFile
        }

        "accept a directory provider" {
            val (project, ext, _) = createFixture()
            val outputDirectory = outDir(project)
            val provider = project.objects.directoryProperty().apply {
                set(outputDirectory)
            }
            ext.outputDirectory(provider)
            ext.outputDirectory shouldContain outputDirectory.get().asFile.absoluteFile
        }

    }

}) {

    companion object {

        const val BASE_PACKAGE = "ch.acanda"

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
