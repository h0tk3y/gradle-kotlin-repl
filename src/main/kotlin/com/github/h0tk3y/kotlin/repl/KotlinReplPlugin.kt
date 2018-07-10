package com.github.h0tk3y.kotlin.repl

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention

class KotlinReplPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val sourceSets = project.pluginManager.withPlugin("java-base") {
            val javaBasePlugin = project.convention.findPlugin(JavaPluginConvention::class.java)
            javaBasePlugin.sourceSets.all { sourceSet ->
                val processor = KotlinReplSourceSetProcessor(project, sourceSet)
                processor.process()
            }
        }
    }
}