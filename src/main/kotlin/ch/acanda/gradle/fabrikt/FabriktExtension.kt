@file:Generated("ch.acanda.gradle.fabrikt.build.ExtensionGenerator")

package ch.acanda.gradle.fabrikt

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import javax.annotation.processing.Generated
import javax.inject.Inject

open class FabriktExtension @Inject constructor(private val project: Project) :
    NamedDomainObjectContainer<GenerateTaskConfiguration> by
    project.objects.domainObjectContainer(GenerateTaskConfiguration::class.java) {

    private var defaultsAction: Action<GenerateTaskDefaults>? = null

    fun defaults(action: Action<GenerateTaskDefaults>) {
        defaultsAction = action
    }

    fun generate(name: String, action: Action<GenerateTaskConfiguration>) {
        register(name, action)
    }

    internal fun getTaskConfigurations(): Provider<List<GenerateTaskConfiguration>> =
        project.provider { map { it.withDefaults() } }

    internal fun getTaskConfiguration(name: String): Provider<GenerateTaskConfiguration> =
        project.provider { getByName(name).withDefaults() }

    private fun GenerateTaskConfiguration.withDefaults(): GenerateTaskConfiguration {
        val defaults = GenerateTaskDefaults(project).also { defaultsAction?.execute(it) }
        return withDefaults(defaults)
    }

}
