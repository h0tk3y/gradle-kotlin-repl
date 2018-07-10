package com.github.h0tk3y.kotlin.repl

import org.jetbrains.kotlin.cli.common.arguments.CommonCompilerArguments
import org.jetbrains.kotlin.compilerRunner.CompilerEnvironment
import org.jetbrains.kotlin.compilerRunner.OutputItemsCollector
import org.jetbrains.kotlin.config.Services
import java.io.File
import java.net.URL
import java.net.URLDecoder
import java.nio.charset.Charset

internal open class GradleCompilerEnvironment(
    val compilerJar: File,
    messageCollector: GradleMessageCollector,
    outputItemsCollector: OutputItemsCollector,
    val compilerArgs: CommonCompilerArguments
) : CompilerEnvironment(Services.EMPTY, messageCollector, outputItemsCollector) {
    val toolsJar: File? by lazy { findToolsJar() }

    val compilerClasspath: List<File>
        get() = listOf(compilerJar, toolsJar).filterNotNull()

    val compilerClasspathURLs: List<URL>
        get() = compilerClasspath.map { it.toURI().toURL() }
}

internal fun findToolsJar(): File? =
    Class.forName("com.sun.tools.javac.util.Context")?.let(::findJarByClass)

private fun findJarByClass(klass: Class<*>): File? {
    val classFileName = klass.name.substringAfterLast(".") + ".class"
    val resource = klass.getResource(classFileName) ?: return null
    val uri = resource.toString()
    if (!uri.startsWith("jar:file:")) return null

    val fileName = URLDecoder.decode(uri.removePrefix("jar:file:").substringBefore("!"), Charset.defaultCharset().name())
    return File(fileName)
}