package com.github.h0tk3y.kotlin.repl

import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.JavaExec
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.compilerRunner.ArgumentUtils
import org.jetbrains.kotlin.compilerRunner.OutputItemsCollectorImpl
import java.io.File

internal val K2JVM_COMPILER_CLASS = "org.jetbrains.kotlin.cli.jvm.K2JVMCompiler"

open class KotlinReplTask() : JavaExec() {
    internal lateinit var classpathProvider: () -> FileCollection
    internal lateinit var compilerJarProvider: () -> File
    internal lateinit var argsProvider: () -> K2JVMCompilerArguments

//    override fun exec() {
//        val javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java"
//        val classpathString = environment.compilerClasspath.map {it.absolutePath}.joinToString(separator = File.pathSeparator)
//        val builder = ProcessBuilder(javaBin, "-cp", classpathString, K2JVM_COMPILER_CLASS,
//            ArgumentUtils.convertArgumentsToStringList(compilerArgs).joinToString(" "))
//
//        val process = launchProcessWithFallback(builder, DaemonReportingTargets(messageCollector = environment.messageCollector))
//
//        // important to read inputStream, otherwise the process may hang on some systems
//        val readErrThread = thread {
//            process.errorStream!!.bufferedReader().forEachLine {
//                System.err.println(it)
//            }
//        }
//        process.inputStream!!.bufferedReader().forEachLine {
//            System.out.println(it)
//        }
//        readErrThread.join()
//
//        val exitCode = process.waitFor()
//    }

    override fun exec() {
        classpath = project.files(environment.compilerClasspath)
        standardInput = System.`in`
        standardOutput = System.`out`
        main = K2JVM_COMPILER_CLASS
        setArgs(ArgumentUtils.convertArgumentsToStringList(compilerArgs))
        super.exec()
    }

    internal val environment by lazy {
        val messageCollector = GradleMessageCollector(logger)
        val outputItemCollector = OutputItemsCollectorImpl()
        GradleCompilerEnvironment(compilerJar, messageCollector, outputItemCollector, compilerArgs)
    }

    //    @get:InputFile
    val compilerJar by lazy { compilerJarProvider() }

    //    @get:Input
    val compilerArgs by lazy {
        argsProvider().apply { buildFile = null }
    }
}