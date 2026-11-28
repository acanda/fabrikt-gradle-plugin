package ch.acanda.gradle.fabrikt.task

import ch.acanda.gradle.fabrikt.FabriktPrintDependenciesTask
import org.gradle.internal.logging.progress.ProgressLogger
import org.gradle.internal.logging.progress.ProgressLoggerFactory

class Progress(factory: ProgressLoggerFactory, val total: Int) : AutoCloseable {

    private val progressLogger: ProgressLogger = factory.newOperation(FabriktPrintDependenciesTask::class.java)
    private var count = 0
    private var failed = false

    init {
        progressLogger.start("Generating Kotlin code with Fabrikt", "[0/$total]")
    }

    fun log(specName: String, skip: Boolean) {
        count++
        val skipMsg = if (skip) " skip" else ""
        progressLogger.progress("[$count/$total]$skipMsg generating code for $specName...")
    }

    fun fail(specName: String) {
        failed = true
        progressLogger.progress("[$count/$total] generating code for $specName...", true)
    }

    override fun close() {
        progressLogger.completed("", failed)
    }

}
