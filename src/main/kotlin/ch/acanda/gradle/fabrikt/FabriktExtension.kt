@file:Generated("ch.acanda.gradle.fabrikt.build.ExtensionGenerator")

package ch.acanda.gradle.fabrikt

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.model.ObjectFactory
import javax.annotation.processing.Generated
import javax.inject.Inject

open class FabriktExtension @Inject constructor(objects: ObjectFactory) :
    NamedDomainObjectContainer<GenerateTaskConfiguration> by
    objects.domainObjectContainer(GenerateTaskConfiguration::class.java) {

    fun generate(name: String, action: Action<GenerateTaskConfiguration>) {
        register(name, action)
    }

}
