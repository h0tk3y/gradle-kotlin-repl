package com.github.h0tk3y.kotlin.repl

import org.gradle.api.logging.Logger
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector

internal class GradleMessageCollector(val logger: Logger) : MessageCollector {
    private var hasErrors = false

    override fun hasErrors() = hasErrors

    override fun clear() {
        // Do nothing
    }

    override fun report(severity: CompilerMessageSeverity, message: String, location: CompilerMessageLocation?) {
        fun formatMsg(prefix: String) =
            buildString {
                append("$prefix: ")

                location?.apply {
                    append("$path: ")
                    if (line > 0 && column > 0) {
                        append("($line, $column): ")
                    }
                }

                append(message)
            }

        when (severity) {
            CompilerMessageSeverity.ERROR,
            CompilerMessageSeverity.EXCEPTION ->  {
                hasErrors = true
                logger.error(formatMsg("e"))
            }

            CompilerMessageSeverity.WARNING,
            CompilerMessageSeverity.STRONG_WARNING -> {
                logger.warn(formatMsg("w"))
            }
            CompilerMessageSeverity.INFO -> {
                logger.info(formatMsg("i"))
            }
            CompilerMessageSeverity.LOGGING,
            CompilerMessageSeverity.OUTPUT -> {
                logger.debug(formatMsg("v"))
            }
        }!! // !! is used to force compile-time exhaustiveness
    }
}
