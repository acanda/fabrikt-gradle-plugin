package ch.acanda.gradle.fabrikt

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import javax.inject.Inject

open class FabriktExtension @Inject constructor(private val project: Project) :
    NamedDomainObjectContainer<GenerateTaskExtension> by
    project.objects.domainObjectContainer(GenerateTaskExtension::class.java) {

    private var defaultsAction: Action<GenerateTaskDefaults>? = null

    fun defaults(action: Action<GenerateTaskDefaults>) {
        defaultsAction = action
    }

    fun generate(name: String, action: Action<GenerateTaskExtension>) {
        register(name, action)
    }

    internal fun getDefaults(): Provider<GenerateTaskDefaults> =
        project.provider {
            project.objects.newInstance(GenerateTaskDefaults::class.java).also { defaultsAction?.execute(it) }
        }

    internal fun getGenerateExtensions(): Provider<out List<GenerateTaskExtension>> =
        project.provider { this.toList() }

    internal fun getGenerateExtension(name: String): Provider<GenerateTaskExtension> =
        project.provider { getByName(name) }

}
