package ch.acanda.gradle.fabrikt

import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import org.gradle.testfixtures.ProjectBuilder

class FabriktPluginTest : WordSpec({

    "The fabrikt plugin" should {
        "register the task fabriktGenerate" {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply("ch.acanda.gradle.fabrikt")
            val task = project.tasks.findByName("fabriktGenerate")
            task.shouldNotBeNull()
        }
    }

})
