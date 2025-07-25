package org.chorus_oss.chorus.plugin

import java.io.File
import java.net.URLClassLoader


class PluginClassLoader(private val loader: JavaPluginLoader, parent: ClassLoader?, file: File) :
    URLClassLoader(arrayOf(file.toURI().toURL()), parent) {
    private val classes: MutableMap<String, Class<*>?> = HashMap()

    @Throws(ClassNotFoundException::class)
    override fun findClass(name: String): Class<*>? {
        return this.findClass(name, true)
    }

    @Throws(ClassNotFoundException::class)
    fun findClass(name: String, checkGlobal: Boolean): Class<*>? {
        var result = classes[name]

        if (result == null) {
            if (checkGlobal) {
                result = loader.getClassByName(name)
            }

            if (result == null) {
                result = super.findClass(name)

                if (result != null) {
                    loader.setClass(name, result)
                }
            }

            classes[name] = result
        }

        return result
    }

    fun getClasses(): Set<String> {
        return classes.keys
    }
}
