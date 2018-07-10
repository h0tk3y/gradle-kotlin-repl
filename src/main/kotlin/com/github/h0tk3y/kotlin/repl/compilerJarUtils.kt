package com.github.h0tk3y.kotlin.repl

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.initialization.dsl.ScriptHandler
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePluginWrapper
import java.io.File
import java.net.URLClassLoader
import java.util.zip.ZipFile

private val KOTLIN_MODULE_GROUP = "org.jetbrains.kotlin"
private val KOTLIN_GRADLE_PLUGIN = "kotlin-gradle-plugin"
private val KOTLIN_COMPILER_EMBEDDABLE = "kotlin-compiler-embeddable"

internal fun findKotlinCompilerJar(project: Project, compilerClassName: String): File? {
    val pluginVersion = pluginVersionFromAppliedPlugin(project)

    val filesToCheck = sequenceOf(pluginVersion?.let(::getCompilerFromClassLoader)) +
        Sequence { findPotentialCompilerJars(project).iterator() } //call the body only when queried
    val entryToFind = compilerClassName.replace(".", "/") + ".class"
    return filesToCheck.filterNotNull().firstOrNull { it.hasEntry(entryToFind) }
}


private fun getCompilerFromClassLoader(pluginVersion: String): File? {
    val urlClassLoader = KotlinReplPlugin::class.java.classLoader as? URLClassLoader ?: return null
    return urlClassLoader.urLs
        .firstOrNull { it.toString().endsWith("kotlin-compiler-embeddable-$pluginVersion.jar") }
        ?.let { File(it.toURI()) }
        ?.takeIf(File::exists)
}

private fun pluginVersionFromAppliedPlugin(project: Project): String? =
    project.plugins.filterIsInstance<KotlinBasePluginWrapper>().firstOrNull()?.kotlinPluginVersion

private fun findPotentialCompilerJars(project: Project): Iterable<File> {
    val projects = generateSequence(project) { it.parent }
    val classpathConfigurations = projects
        .map { it.buildscript.configurations.findByName(ScriptHandler.CLASSPATH_CONFIGURATION) }
        .filterNotNull()

    val allFiles = HashSet<File>()

    for (configuration in classpathConfigurations) {
        val compilerEmbeddable = findCompilerEmbeddable(configuration)

        if (compilerEmbeddable != null) {
            return compilerEmbeddable.moduleArtifacts.map { it.file }
        }
        else {
            allFiles.addAll(configuration.files)
        }
    }

    return allFiles
}

private fun findCompilerEmbeddable(configuration: Configuration): ResolvedDependency? {
    fun Iterable<ResolvedDependency>.findDependency(group: String, name: String): ResolvedDependency? =
        find { it.moduleGroup == group && it.moduleName == name }

    val firstLevelModuleDependencies = configuration.resolvedConfiguration.firstLevelModuleDependencies
    val gradlePlugin = firstLevelModuleDependencies.findDependency(KOTLIN_MODULE_GROUP, KOTLIN_GRADLE_PLUGIN)
    return gradlePlugin?.children?.findDependency(KOTLIN_MODULE_GROUP, KOTLIN_COMPILER_EMBEDDABLE)
}

private fun File.hasEntry(entryToFind: String): Boolean {
    val zip = ZipFile(this)

    try {
        return zip.getEntry(entryToFind) != null
    }
    catch (e: Exception) {
        return false
    }
    finally {
        zip.close()
    }
}