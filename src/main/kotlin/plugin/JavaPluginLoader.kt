package org.chorus_oss.chorus.plugin

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.event.plugin.PluginDisableEvent
import org.chorus_oss.chorus.event.plugin.PluginEnableEvent
import org.chorus_oss.chorus.utils.Loggable
import org.chorus_oss.chorus.utils.PluginException
import org.chorus_oss.chorus.utils.Utils


import java.io.File
import java.io.IOException
import java.util.jar.JarFile
import java.util.regex.Pattern

class JavaPluginLoader(private val server: Server) : PluginLoader {
    private val classes: MutableMap<String, Class<*>> = HashMap()
    private val classLoaders: MutableMap<String?, PluginClassLoader> = HashMap()

    @Throws(Exception::class)
    override fun loadPlugin(file: File): Plugin? {
        val description = this.getPluginDescription(file)
        if (description != null) {
            log.info(server.lang.tr("chorus.plugin.load", description.versionedName))
            val dataFolder = File(file.parentFile, description.name)
            check(!(dataFolder.exists() && !dataFolder.isDirectory)) { "Projected dataFolder '" + dataFolder + "' for " + description.name + " exists and is not a directory" }

            val className = description.main
            val classLoader = PluginClassLoader(this, javaClass.classLoader, file)
            classLoaders[description.name] = classLoader
            val plugin: PluginBase
            try {
                val javaClass = classLoader.loadClass(className)

                if (!PluginBase::class.java.isAssignableFrom(javaClass)) {
                    throw PluginException("Main class `" + description.main + "' does not extend PluginBase")
                }

                try {
                    val pluginClass = javaClass.asSubclass(PluginBase::class.java) as Class<PluginBase>

                    plugin = pluginClass.getDeclaredConstructor().newInstance()
                    this.initPlugin(plugin, classLoader, description, dataFolder, file)

                    return plugin
                } catch (e: ClassCastException) {
                    throw PluginException("Error whilst initializing main class `" + description.main + "'", e)
                } catch (e: InstantiationException) {
                    log.error(
                        "An exception happened while initializing the plugin {}, {}, {}, {}",
                        file,
                        className,
                        description.name,
                        description.version,
                        e
                    )
                } catch (e: IllegalAccessException) {
                    log.error(
                        "An exception happened while initializing the plugin {}, {}, {}, {}",
                        file,
                        className,
                        description.name,
                        description.version,
                        e
                    )
                }
            } catch (_: ClassNotFoundException) {
                throw PluginException("Couldn't load plugin " + description.name + ": main class not found")
            }
        }

        return null
    }

    @Throws(Exception::class)
    override fun loadPlugin(filename: String): Plugin? {
        return this.loadPlugin(File(filename))
    }

    override fun getPluginDescription(file: File): PluginTOML? {
        try {
            JarFile(file).use { jar ->
                val entry = jar.getJarEntry("plugin.toml") ?: return null
                jar.getInputStream(entry).use { stream ->
                    return PluginTOML.fromString(Utils.readFile(stream))
                }
            }
        } catch (e: IOException) {
            return null
        }
    }

    override fun getPluginDescription(filename: String): PluginTOML? {
        return this.getPluginDescription(File(filename))
    }

    override val pluginFilters: Array<Pattern>
        get() = arrayOf(Pattern.compile("^.+\\.jar$"))

    private fun initPlugin(
        plugin: PluginBase,
        classLoader: ClassLoader,
        description: PluginTOML,
        dataFolder: File,
        file: File
    ) {
        plugin.init(this, classLoader, this.server, description, dataFolder, file)
        plugin.onLoad()
    }

    override fun enablePlugin(plugin: Plugin) {
        if (plugin is PluginBase && !plugin.isEnabled) {
            log.info(server.lang.tr("chorus.plugin.enable", plugin.description.versionedName))
            plugin.isEnabled = true
            server.pluginManager.callEvent(PluginEnableEvent(plugin))
        }
    }


    override fun disablePlugin(plugin: Plugin) {
        if (plugin is PluginBase && plugin.isEnabled) {
            if (plugin === InternalPlugin.Companion.INSTANCE) {
                throw UnsupportedOperationException("The PowerNukkitX Internal Plugin cannot be disabled")
            }

            log.info(server.lang.tr("chorus.plugin.disable", plugin.description.versionedName))

            server.pluginManager.callEvent(PluginDisableEvent(plugin))

            plugin.isEnabled = false
        }
    }

    fun getClassByName(name: String): Class<*>? {
        var cachedClass = classes[name]

        if (cachedClass != null) {
            return cachedClass
        } else {
            for (loader in classLoaders.values) {
                try {
                    cachedClass = loader.findClass(name, false)
                } catch (e: ClassNotFoundException) {
                    //ignore
                }
                if (cachedClass != null) {
                    return cachedClass
                }
            }
        }
        return null
    }

    fun setClass(name: String, clazz: Class<*>) {
        if (!classes.containsKey(name)) {
            classes[name] = clazz
        }
    }

    private fun removeClass(name: String) {
        classes.remove(name)!!
    }

    companion object : Loggable
}
