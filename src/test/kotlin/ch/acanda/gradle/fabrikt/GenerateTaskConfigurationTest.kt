package ch.acanda.gradle.fabrikt

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested
import org.gradle.testfixtures.ProjectBuilder
import kotlin.reflect.KVisibility
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberProperties

class GenerateTaskConfigurationTest : StringSpec({

    val project = ProjectBuilder.builder().build()
    checkProperties(GenerateTaskConfiguration("dog", project.objects))

}) {

    companion object {
        private fun StringSpec.checkProperties(config: Any) {
            val name = config::class.simpleName?.replace("_Decorated", "")
            config::class.memberProperties
                .filter { it.getter.visibility != KVisibility.PRIVATE }
                .forEach { prop ->
                    val getter = prop.getter
                    val propValue = getter.call(config)
                    if (propValue is Property<*>) {
                        "$name.${prop.name} should not have a convention value." {
                            withClue("The convention should be set in GenerateTaskConfigurationDefaults.") {
                                propValue.isPresent shouldBe false
                            }
                        }
                    } else if (getter.hasAnnotation<Nested>() && propValue != null) {
                        checkProperties(propValue)
                    }
                }
        }
    }

}
