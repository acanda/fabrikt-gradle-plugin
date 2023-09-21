package ch.acanda.gradle.fabrikt.generator

import ch.acanda.gradle.fabrikt.GenerateTaskConfiguration
import com.cjbooms.fabrikt.cli.CodeGenerationType

internal const val ARG_API_FILE = "--api-file"
internal const val ARG_BASE_PACKAGE = "--base-package"
internal const val ARG_OUT_DIR = "--output-directory"
internal const val ARG_SRC_PATH = "--src-path"
internal const val ARG_RESOURCES_PATH = "--resources-path"
internal const val ARG_API_FRAGMENT = "--api-fragment"
internal const val ARG_TARGETS = "--targets"
internal const val ARG_CLIENT_OPTS = "--http-client-opts"
internal const val ARG_CLIENT_TARGET = "--http-client-target"
internal const val ARG_CONTROLLER_OPTS = "--http-controller-opts"
internal const val ARG_CONTROLLER_TARGET = "--http-controller-target"
internal const val ARG_MODEL_OPTS = "--http-model-opts"

internal data class FabriktArguments(private val config: GenerateTaskConfiguration) {

    fun getCliArgs(): Array<String> = with(config) {
        @Suppress("ArgumentListWrapping")
        val args = mutableListOf(
            ARG_API_FILE, apiFile.asFile.get().absolutePath,
            ARG_BASE_PACKAGE, basePackage.get().toString(),
            ARG_OUT_DIR, outputDirectory.asFile.get().absolutePath,
        )
        apiFragments.forEach { fragment ->
            args.add(ARG_API_FRAGMENT)
            args.add(fragment.absolutePath)
        }
        sourcesPath.orNull?.let { path ->
            args.add(ARG_SRC_PATH)
            args.add(path.toString())
        }
        resourcesPath.orNull?.let { path ->
            args.add(ARG_RESOURCES_PATH)
            args.add(path.toString())
        }
        args.add(ARG_TARGETS)
        args.add(CodeGenerationType.QUARKUS_REFLECTION_CONFIG.name)
        addClientArgs(args)
        addControllerArgs(args)
        addModelArgs(args)
        return args.toTypedArray()
    }

    private fun GenerateTaskConfiguration.addClientArgs(args: MutableList<String>) = with(client) {
        if (enabled.get()) {
            args.add(ARG_TARGETS)
            args.add(CodeGenerationType.CLIENT.name)
            options.get().forEach { option ->
                args.add(ARG_CLIENT_OPTS)
                args.add(option.name)
            }
            target.orNull?.let {
                args.add(ARG_CLIENT_TARGET)
                args.add(it.name)
            }
        }
    }

    private fun GenerateTaskConfiguration.addControllerArgs(args: MutableList<String>) = with(controller) {
        if (enabled.get()) {
            args.add(ARG_TARGETS)
            args.add(CodeGenerationType.CONTROLLERS.name)
            options.get().forEach { option ->
                args.add(ARG_CONTROLLER_OPTS)
                args.add(option.name)
            }
            target.orNull?.let {
                args.add(ARG_CONTROLLER_TARGET)
                args.add(it.name)
            }
        }
    }

    private fun GenerateTaskConfiguration.addModelArgs(args: MutableList<String>) = with(model) {
        if (enabled.get()) {
            args.add(ARG_TARGETS)
            args.add(CodeGenerationType.HTTP_MODELS.name)
            options.get().forEach { option ->
                args.add(ARG_MODEL_OPTS)
                args.add(option.name)
            }
        }
    }

}
