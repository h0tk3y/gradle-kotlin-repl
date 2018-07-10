package com.github.h0tk3y.kotlin.repl

import java.net.URL
import java.net.URLClassLoader

/**
 * A parent-last classloader that will try the child classloader first and then the parent.
 * This takes a fair bit of doing because java really prefers parent-first.
 *
 *
 * For those not familiar with class loading trickery, be wary
 *
 * http://stackoverflow.com/questions/5445511/how-do-i-create-a-parent-last-child-first-classloader-in-java-or-how-to-overr
 */
class ParentLastURLClassLoader(classpath: List<URL>, parent: ClassLoader?) : ClassLoader(Thread.currentThread().contextClassLoader) {
    private val childClassLoader: ChildURLClassLoader

    init {

        val urls = classpath.toTypedArray()

        childClassLoader = ChildURLClassLoader(urls, FindClassClassLoader(parent))
    }

    @Synchronized
    @Throws(ClassNotFoundException::class)
    override fun loadClass(name: String, resolve: Boolean): Class<*> {
        try {
            // first we try to find a class inside the child classloader
            return childClassLoader.findClass(name)
        } catch (e: ClassNotFoundException) {
            // didn't find it, try the parent
            return super.loadClass(name, resolve)
        }

    }

    /**
     * This class allows me to call findClass on a classloader
     */
    internal class FindClassClassLoader(parent: ClassLoader?) : ClassLoader(parent) {

        @Throws(ClassNotFoundException::class)
        public override fun findClass(name: String): Class<*> {
            return super.findClass(name)
        }
    }

    /**
     * This class delegates (child then parent) for the findClass method for a URLClassLoader.
     * We need this because findClass is protected in URLClassLoader
     */
    internal class ChildURLClassLoader(urls: Array<URL>, private val realParent: FindClassClassLoader) : URLClassLoader(urls, null) {


        @Throws(ClassNotFoundException::class)
        public override fun findClass(name: String): Class<*> {
            val loaded = findLoadedClass(name)
            if (loaded != null) {
                return loaded
            }

            try {
                return super.findClass(name)
            } catch (e: ClassNotFoundException) {
                // if that fails, we ask our real parent classloader to load the class (we give up)
                return realParent.loadClass(name)
            }

        }
    }
}