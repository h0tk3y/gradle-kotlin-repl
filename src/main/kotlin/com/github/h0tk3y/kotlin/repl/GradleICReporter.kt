package com.github.h0tk3y.kotlin.repl

import org.gradle.api.logging.Logging
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.incremental.ICReporter
import java.io.File

internal class GradleICReporter(private val projectRootFile: File) : ICReporter {
    private val log = Logging.getLogger(GradleICReporter::class.java)

    override fun report(message: ()->String) {
        log.debug("[KOTLIN] $message")
    }

    override fun pathsAsString(files: Iterable<File>): String =
        files.map { it.absolutePath }.joinToString()

    override fun reportCompileIteration(sourceFiles: Collection<File>, exitCode: ExitCode) {
        if (sourceFiles.any()) {
            report { "compile iteration: ${pathsAsString(sourceFiles)}" }
        }
        report { "compiler exit code: $exitCode" }
    }
}