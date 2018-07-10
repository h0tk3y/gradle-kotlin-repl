package com.github.h0tk3y.kotlin.repl

import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File

class KotlinReplSourceSetProcessor(
    val project: Project,
    val sourceSet: SourceSet
) {
    fun process() {
        project.afterEvaluate {
            val kotlinTask =
                project.tasks.findByName(sourceSet.getTaskName("compile", "Kotlin")) as KotlinCompile

            val compileAndOutputClasspathProvider = {
                sourceSet.compileClasspath + sourceSet.output.classesDirs
            }

            val kotlinCompileJarProvider = {
                kotlinTask.compilerJarFile
                    ?: findKotlinCompilerJar(project, K2JVM_COMPILER_CLASS)
                    ?: throw IllegalStateException("Could not find Kotlin Compiler JAR.")
            }

            val kotlinCompileArgsProvider = {
                K2JVMCompilerArguments().also {
                    kotlinTask.setupCompilerArgs(it)
                    it.classpath = compileAndOutputClasspathProvider().joinToString(File.pathSeparator)
                }
            }

            val taskName = sourceSet.getTaskName("run", "KotlinRepl")
            val replTask = project.tasks.create(taskName, KotlinReplTask::class.java).apply {
                classpathProvider = compileAndOutputClasspathProvider
                compilerJarProvider = kotlinCompileJarProvider
                argsProvider = kotlinCompileArgsProvider
            }

            replTask.dependsOn(kotlinTask)
        }
    }
}