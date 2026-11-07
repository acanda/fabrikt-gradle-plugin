package ch.acanda.gradle.fabrikt.task

import ch.acanda.gradle.fabrikt.generator.GeneratorException
import ch.acanda.gradle.fabrikt.generator.ValidationException
import ch.acanda.gradle.fabrikt.generator.ValidationProblem
import ch.acanda.gradle.fabrikt.generator.ValidationSeverity
import org.gradle.api.Action
import org.gradle.api.file.RegularFile
import org.gradle.api.problems.ProblemGroup
import org.gradle.api.problems.ProblemId
import org.gradle.api.problems.ProblemSpec
import org.gradle.api.problems.Problems
import org.gradle.internal.service.ServiceRegistry

@Suppress("UnstableApiUsage")
private val PROBLEM_GROUP = ProblemGroup.create("fabrikt", "Fabrikt Code Generation")

@Suppress("UnstableApiUsage")
fun handleGeneratorException(
    e: GeneratorException,
    specFile: RegularFile,
    configName: String,
    services: ServiceRegistry
) {
    val problemReporter = services.get(Problems::class.java).reporter
    val id = ProblemId.create("generator", "Fabrikt failed to generate code.", PROBLEM_GROUP)
    problemReporter.throwing(e, id, generatorProblem(specFile, configName))
}

@Suppress("UnstableApiUsage")
fun handleValidationException(e: ValidationException, configName: String, services: ServiceRegistry) {
    val problemReporter = services.get(Problems::class.java).reporter
    val id = ProblemId.create("validation", "Invalid value in Fabrikt settings.", PROBLEM_GROUP)
    e.problems.forEach { problem ->
        when (problem.severity) {
            ValidationSeverity.WARNING -> problemReporter.report(id, validationProblem(problem, configName))
            ValidationSeverity.ERROR -> problemReporter.throwing(e, id, validationProblem(problem, configName))
        }
    }
}

@Suppress("UnstableApiUsage")
private fun generatorProblem(specFile: RegularFile, name: String) =
    Action { problem: ProblemSpec ->
        problem
            .contextualLabel("Fabrikt failed to generate code for configuration $name.")
            .details("Fabrikt failed to generate code for the specification $specFile.")
    }

@Suppress("UnstableApiUsage")
private fun validationProblem(e: ValidationProblem, name: String) =
    Action { problem: ProblemSpec ->
        problem
            .contextualLabel("Invalid Fabrikt setting in configuration $name.")
            .details(e.message)
    }
