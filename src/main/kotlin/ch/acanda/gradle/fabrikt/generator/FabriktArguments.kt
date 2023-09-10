package ch.acanda.gradle.fabrikt.generator

import ch.acanda.gradle.fabrikt.GenerateTaskConfiguration
import com.cjbooms.fabrikt.cli.CodeGenerationType

internal const val ARG_API_FILE = "--api-file"
internal const val ARG_BASE_PACKAGE = "--base-package"
internal const val ARG_OUT_DIR = "--output-directory"
internal const val ARG_API_FRAGMENT = "--api-fragment"
internal const val ARG_TARGETS = "--targets"
internal const val ARG_CLIENT_OPTS = "--http-client-opts"
internal const val ARG_CLIENT_TARGET = "--http-client-target"

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
        targets.get().filterNot { it == CodeGenerationType.CLIENT }.forEach { target ->
            args.add(ARG_TARGETS)
            args.add(target.name)
        }
        addClientArgs(args)
        return args.toTypedArray()
    }

    private fun GenerateTaskConfiguration.addClientArgs(args: MutableList<String>) {
        with(client) {
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
    }
}
