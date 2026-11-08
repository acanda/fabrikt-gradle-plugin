package ch.acanda.gradle.fabrikt.task

import ch.acanda.gradle.fabrikt.generator.GeneratorException
import ch.acanda.gradle.fabrikt.generator.ValidationException
import ch.acanda.gradle.fabrikt.generator.ValidationProblem
import ch.acanda.gradle.fabrikt.generator.ValidationSeverity
import org.gradle.api.Action
import org.gradle.api.file.RegularFile
import org.gradle.api.problems.ProblemSpec
import org.gradle.api.problems.Problems

@Suppress("UnstableApiUsage")
fun handleGeneratorException(
    e: GeneratorException,
    specFile: RegularFile,
    configName: String,
    problems: Problems
) {
    val group = problems.groups.transformation
    val id = group.codeTransformation.problemId("Fabrikt failed to generate code.")
    problems.reporter.throwing(e, id, generatorProblem(specFile, configName))
}

@Suppress("UnstableApiUsage")
fun handleValidationException(e: ValidationException, configName: String, problems: Problems) {
    val group = problems.groups.gradle.pluginValidation
    val id = group.problemId("Invalid value in Fabrikt settings.")
    e.problems.forEach { problem ->
        when (problem.severity) {
            ValidationSeverity.WARNING -> problems.reporter.report(id, validationProblem(problem, configName))
            ValidationSeverity.ERROR -> problems.reporter.throwing(e, id, validationProblem(problem, configName))
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
