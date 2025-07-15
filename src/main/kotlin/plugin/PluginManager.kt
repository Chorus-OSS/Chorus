package org.chorus_oss.chorus.plugin


import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.command.SimpleCommandMap
import org.chorus_oss.chorus.event.*
import org.chorus_oss.chorus.event.HandlerList.Companion.unregisterAll
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.permission.Permissible
import org.chorus_oss.chorus.permission.Permission
import org.chorus_oss.chorus.utils.Loggable
import org.chorus_oss.chorus.utils.PluginException
import org.chorus_oss.chorus.utils.Utils
import java.io.File
import java.lang.Deprecated
import java.lang.reflect.Method
import java.util.*
import java.util.function.Consumer
import java.util.regex.Pattern
import kotlin.Array
import kotlin.Boolean
import kotlin.Exception
import kotlin.IllegalArgumentException
import kotlin.NullPointerException
import kotlin.String
import kotlin.Throwable
import kotlin.Throws
import kotlin.UnsupportedOperationException
import kotlin.addSuppressed
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.jvmName
import kotlin.require

open class PluginManager(private val server: Server, private val commandMap: SimpleCommandMap) {
    val plugins: MutableMap<String, Plugin> = LinkedHashMap()

    protected val permissions: MutableMap<String, Permission> = HashMap()

    protected val defaultPerms: MutableMap<String, Permission> = HashMap()

    protected val defaultPermsOp: MutableMap<String, Permission> = HashMap()

    protected val permSubs: MutableMap<String, MutableSet<Permissible>> = HashMap()

    protected val defSubs: MutableSet<Permissible> = Collections.newSetFromMap(WeakHashMap())

    protected val defSubsOp: MutableSet<Permissible> = Collections.newSetFromMap(WeakHashMap())


    protected val fileAssociations: MutableMap<String, PluginLoader> = HashMap()

    fun getPlugin(name: String): Plugin? {
        if (plugins.containsKey(name)) {
            return plugins[name]
        }
        return null
    }

    fun registerInterface(loaderClass: Class<out PluginLoader>?): Boolean {
        if (loaderClass != null) {
            try {
                val constructor = loaderClass.getDeclaredConstructor(
                    Server::class.java
                )
                constructor.isAccessible = true
                fileAssociations[loaderClass.name] = constructor.newInstance(this.server)
                return true
            } catch (e: Exception) {
                return false
            }
        }
        return false
    }

    fun loadInternalPlugin() {
        val pluginLoader = fileAssociations[JavaPluginLoader::class.java.name]!!
        val plugin: InternalPlugin = InternalPlugin.Companion.INSTANCE
        val file = try {
            File(Server::class.java.protectionDomain.codeSource.location.toURI())
        } catch (_: Exception) {
            File(".")
        }

        val description = PluginTOML(
            name = "Chorus",
            main = InternalPlugin::class.java.name,
            version = server.chorusVersion,
            api = emptyList(),
            description = "https://github.com/Chorus-OSS/Chorus",
        )

        plugin.init(
            pluginLoader,
            javaClass.classLoader, server, description, File("Chorus"), file
        )
        plugins[description.name] = plugin
        enablePlugin(plugin)
    }

    fun loadPlugin(file: File): Plugin? {
        return this.loadPlugin(file, null)
    }

    @JvmOverloads
    fun loadPlugin(path: String, loaders: Map<String, PluginLoader>? = null): Plugin? {
        return this.loadPlugin(File(path), loaders)
    }

    fun loadPlugin(file: File, loaders: Map<String, PluginLoader>?): Plugin? {
        for (loader in (loaders ?: this.fileAssociations).values) {
            for (pattern in loader.pluginFilters) {
                if (pattern.matcher(file.name).matches()) {
                    val description = loader.getPluginDescription(file)
                    if (description != null) {
                        try {
                            val plugin = loader.loadPlugin(file)
                            if (plugin != null) {
                                plugins[plugin.description.name] = plugin
                                return plugin
                            }
                        } catch (e: Exception) {
                            log.error("Could not load plugin", e)
                            return null
                        }
                    }
                }
            }
        }

        return null
    }

    fun loadPlugins(dictionary: String): Map<String, Plugin> {
        return this.loadPlugins(File(dictionary))
    }

    fun loadPlugins(dictionary: String, newLoaders: List<String>?): Map<String, Plugin> {
        return this.loadPlugins(File(dictionary), newLoaders)
    }

    @JvmOverloads
    fun loadPlugins(dictionary: File, newLoaders: List<String>? = null): Map<String, Plugin> {
        return this.loadPlugins(dictionary, newLoaders, false)
    }

    fun loadPlugins(dictionary: File, newLoaders: List<String>?, includeDir: Boolean): Map<String, Plugin> {
        if (dictionary.isDirectory) {
            val plugins: MutableMap<String, File> = LinkedHashMap()
            val loadedPlugins: MutableMap<String, Plugin> = LinkedHashMap()
            val dependencies: MutableMap<String, MutableList<String>?> = LinkedHashMap()
            val softDependencies: MutableMap<String, MutableList<String>?> = LinkedHashMap()
            var loaders: MutableMap<String, PluginLoader> = LinkedHashMap()
            if (newLoaders != null) {
                for (key in newLoaders) {
                    if (fileAssociations.containsKey(key)) {
                        loaders[key] = fileAssociations[key]!!
                    }
                }
            } else {
                loaders = this.fileAssociations
            }

            for (loader in loaders.values) {
                for (file in Objects.requireNonNull<Array<File>>(dictionary.listFiles { _, name ->
                    for (pattern in loader.pluginFilters) {
                        if (pattern.matcher(name).matches()) {
                            return@listFiles true
                        }
                    }
                    false
                })) {
                    if ((file.isDirectory && !file.name.startsWith("@")) && !includeDir) {
                        continue
                    }
                    try {
                        val description = loader.getPluginDescription(file)
                        if (description != null) {
                            val name = description.name

                            if (plugins.containsKey(name) || this.getPlugin(name) != null) {
                                log.error(
                                    server.lang.tr(
                                        "chorus.plugin.duplicateError",
                                        name
                                    )
                                )
                                continue
                            }

                            var compatible = 0

                            for (version in description.api) {
                                try {
                                    //Check the format: majorVersion.minorVersion.patch
                                    require(
                                        Pattern.matches(
                                            "^[0-9]+\\.[0-9]+\\.[0-9]+$",
                                            version
                                        )
                                    ) { "The getCompatibleAPI version don't match the format majorVersion.minorVersion.patch" }
                                } catch (e: NullPointerException) {
                                    log.error(
                                        server.lang.tr(
                                            "chorus.plugin.loadError",
                                            name, "Wrong API format"
                                        ), e
                                    )
                                    continue
                                } catch (e: IllegalArgumentException) {
                                    log.error(
                                        server.lang.tr(
                                            "chorus.plugin.loadError",
                                            name, "Wrong API format"
                                        ), e
                                    )
                                    continue
                                }

                                val versionArray =
                                    version.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                                val apiVersion =
                                    server.apiVersion.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }
                                        .toTypedArray()

                                //Completely different API version
                                if (versionArray[0].toInt() != apiVersion[0].toInt()) {
                                    compatible = 1
                                    break
                                }

                                //If the plugin requires new API features, being backwards compatible
                                if (versionArray[1].toInt() > apiVersion[1].toInt()) {
                                    compatible = 2
                                    continue
                                }
                                break
                            }

                            if (compatible > 0) {
                                log.error(
                                    server.lang.tr(
                                        "chorus.plugin.loadError",
                                        name, "%nukkit.plugin.incompatibleAPI"
                                    )
                                )
                                if (compatible == 1) {
                                    log.error("The major version is not compatible, and the plugin will not load!")
                                    continue
                                }
                            }

                            plugins[name] = file

                            softDependencies[name] = description.softDependencies

                            dependencies[name] = description.dependencies

                            for (before in description.loadBefore) {
                                if (softDependencies.containsKey(before)) {
                                    softDependencies[before]!!.add(name)
                                } else {
                                    val list: MutableList<String> = ArrayList()
                                    list.add(name)
                                    softDependencies[before] = list
                                }
                            }
                        }
                    } catch (e: Exception) {
                        log.error(
                            server.lang.tr(
                                "nukkit.plugin" +
                                        ".fileError", file.name, dictionary.toString(), Utils
                                    .getExceptionMessage(e)
                            ), e
                        )
                    }
                }
            }

            while (!plugins.isEmpty()) {
                var missingDependency = true
                for (name in ArrayList(plugins.keys)) {
                    val file = plugins[name]
                    if (dependencies.containsKey(name)) {
                        for (dependency in ArrayList(dependencies[name]!!)) {
                            if (loadedPlugins.containsKey(dependency) || this.getPlugin(dependency) != null) {
                                dependencies[name]!!.remove(dependency)
                            } else if (!plugins.containsKey(dependency)) {
                                val language = server.lang
                                val cause = language.tr("chorus.plugin.missingDependency", dependency)
                                log.error(language.tr("chorus.plugin.loadError", name, cause))
                                break
                            }
                        }

                        if (dependencies[name]!!.isEmpty()) {
                            dependencies.remove(name)
                        }
                    }

                    if (softDependencies.containsKey(name)) {
                        softDependencies[name]!!.removeIf { dependency ->
                            loadedPlugins.containsKey(dependency) || this.getPlugin(
                                dependency
                            ) != null
                        }

                        if (softDependencies[name]!!.isEmpty()) {
                            softDependencies.remove(name)
                        }
                    }

                    if (!dependencies.containsKey(name) && !softDependencies.containsKey(name)) {
                        plugins.remove(name)
                        missingDependency = false
                        val plugin = this.loadPlugin(file!!, loaders)
                        if (plugin != null) {
                            loadedPlugins[name] = plugin
                        } else {
                            log.error(server.lang.tr("chorus.plugin.genericLoadError", name))
                        }
                    }
                }

                if (missingDependency) {
                    for (name in ArrayList<String>(plugins.keys)) {
                        val file = plugins[name]
                        if (!dependencies.containsKey(name)) {
                            softDependencies.remove(name)
                            plugins.remove(name)
                            missingDependency = false
                            val plugin = this.loadPlugin(file!!, loaders)
                            if (plugin != null) {
                                loadedPlugins[name] = plugin
                            } else {
                                log.error(server.lang.tr("chorus.plugin.genericLoadError", name))
                            }
                        }
                    }

                    if (missingDependency) {
                        for (name in plugins.keys) {
                            log.error(
                                server.lang.tr(
                                    "chorus.plugin.loadError",
                                    name,
                                    "%nukkit.plugin.circularDependency"
                                )
                            )
                        }
                        plugins.clear()
                    }
                }
            }

            return loadedPlugins
        } else {
            return HashMap()
        }
    }

    fun getPermission(name: String): Permission? {
        if (permissions.containsKey(name)) {
            return permissions[name]
        }
        return null
    }

    fun addPermission(permission: Permission): Boolean {
        if (!permissions.containsKey(permission.name)) {
            permissions[permission.name] = permission
            this.calculatePermissionDefault(permission)

            return true
        }

        return false
    }

    fun removePermission(name: String) {
        permissions.remove(name)
    }

    fun removePermission(permission: Permission) {
        this.removePermission(permission.name)
    }

    fun getDefaultPermissions(op: Boolean): Map<String, Permission> {
        return if (op) {
            defaultPermsOp
        } else {
            defaultPerms
        }
    }

    fun recalculatePermissionDefaults(permission: Permission) {
        if (permissions.containsKey(permission.name)) {
            defaultPermsOp.remove(permission.name)
            defaultPerms.remove(permission.name)
            this.calculatePermissionDefault(permission)
        }
    }

    private fun calculatePermissionDefault(permission: Permission) {
        if (permission.default == Permission.DEFAULT_OP || permission.default == Permission.DEFAULT_TRUE) {
            defaultPermsOp[permission.name] = permission
            this.dirtyPermissibles(true)
        }

        if (permission.default == Permission.DEFAULT_NOT_OP || permission.default == Permission.DEFAULT_TRUE) {
            defaultPerms[permission.name] = permission
            this.dirtyPermissibles(false)
        }
    }

    private fun dirtyPermissibles(op: Boolean) {
        for (p in this.getDefaultPermSubscriptions(op)) {
            p.recalculatePermissions()
        }
    }

    fun subscribeToPermission(permission: String, permissible: Permissible) {
        if (!permSubs.containsKey(permission)) {
            permSubs[permission] =
                Collections.newSetFromMap(WeakHashMap())
        }
        permSubs[permission]!!.add(permissible)
    }

    fun unsubscribeFromPermission(permission: String, permissible: Permissible) {
        if (permSubs.containsKey(permission)) {
            permSubs[permission]!!.remove(permissible)
            if (permSubs[permission]!!.isEmpty()) {
                permSubs.remove(permission)
            }
        }
    }

    fun getPermissionSubscriptions(permission: String): MutableSet<Permissible> {
        if (permSubs.containsKey(permission)) {
            return HashSet(permSubs[permission]!!)
        }
        return HashSet()
    }

    fun subscribeToDefaultPerms(op: Boolean, permissible: Permissible) {
        if (op) {
            defSubsOp.add(permissible)
        } else {
            defSubs.add(permissible)
        }
    }

    fun unsubscribeFromDefaultPerms(op: Boolean, permissible: Permissible) {
        if (op) {
            defSubsOp.remove(permissible)
        } else {
            defSubs.remove(permissible)
        }
    }

    fun getDefaultPermSubscriptions(op: Boolean): Set<Permissible> {
        return if (op) {
            HashSet(this.defSubsOp)
        } else {
            HashSet(this.defSubs)
        }
    }

    fun isPluginEnabled(plugin: Plugin?): Boolean {
        return if (plugin != null && plugins.containsKey(plugin.description.name)) {
            plugin.isEnabled
        } else {
            false
        }
    }

    fun enablePlugin(plugin: Plugin) {
        if (!plugin.isEnabled) {
            try {
                for (permission in plugin.description.permissions) {
                    this.addPermission(permission)
                }
                plugin.pluginLoader.enablePlugin(plugin)
            } catch (e: Throwable) {
                log.error(
                    "An error occurred while enabling the plugin {}, {}, {}",
                    plugin.description.name, plugin.description.version, plugin.description.main, e
                )
                this.disablePlugin(plugin)
            }
        }
    }

    fun disablePlugins() {
        val plugins: ListIterator<Plugin> = ArrayList(
            plugins.values
        ).listIterator(plugins.size)

        while (plugins.hasPrevious()) {
            val previous = plugins.previous()
            if (previous !== InternalPlugin.Companion.INSTANCE) {
                this.disablePlugin(previous)
            }
        }
    }

    fun disablePlugin(plugin: Plugin) {
        if (InternalPlugin.Companion.INSTANCE == plugin) {
            throw UnsupportedOperationException("The PowerNukkitX Internal plugin can't be disabled.")
        }

        if (plugin.isEnabled) {
            try {
                plugin.pluginLoader.disablePlugin(plugin)
            } catch (e: Exception) {
                log.error(
                    "An error occurred while disabling the plugin {}, {}, {}",
                    plugin.description.name, plugin.description.version, plugin.description.main, e
                )
            }

            server.scheduler.cancelTask(plugin)
            server.levels.values.forEach(Consumer { level: Level -> level.scheduler.cancelTask(plugin) })
            unregisterAll(plugin)
            for (permission in plugin.description.permissions) {
                this.removePermission(permission)
            }
        }
    }

    fun clearPlugins() {
        this.disablePlugins()
        plugins.clear()
        fileAssociations.clear()
        permissions.clear()
        defaultPerms.clear()
        defaultPermsOp.clear()
    }

    open fun callEvent(event: Event) {
        //Used for event listeners inside command blocks
        try {
            for (registration in getEventListeners(event.javaClass).registeredListeners) {
                if (!registration.plugin.isEnabled) {
                    continue
                }

                try {
                    registration.callEvent(event)
                } catch (e: Exception) {
                    log.error(
                        server.lang.tr(
                            "chorus.plugin.eventError",
                            event.getSafeName(),
                            registration.plugin.description.versionedName,
                            e.message!!,
                            registration.listener.javaClass.name
                        ), e
                    )
                }
            }
        } catch (e: IllegalAccessException) {
            log.error("An error has occurred while calling the event {}", event, e)
        }
    }

    fun registerEvents(listener: Listener, plugin: Plugin) {
        if (!plugin.isEnabled) {
            throw PluginException("Plugin attempted to register " + listener.javaClass.name + " while not enabled")
        }

        val methods: Set<Method>
        try {
            val publicMethods = listener.javaClass.methods
            val privateMethods = listener.javaClass.declaredMethods
            methods = HashSet(publicMethods.size + privateMethods.size, 1.0f)
            Collections.addAll(methods, *publicMethods)
            Collections.addAll(methods, *privateMethods)
        } catch (e: NoClassDefFoundError) {
            plugin.logger.error("Plugin " + plugin.description.versionedName + " has failed to register events for " + listener.javaClass + " because " + e.message + " does not exist.")
            return
        }

        for (method in methods) {
            val eh = method.getAnnotation(EventHandler::class.java) ?: continue
            if (method.isBridge || method.isSynthetic) {
                continue
            }
            val checkClass: Class<*> = method.parameterTypes[0]
            if (method.parameterTypes.size != 1 || !Event::class.java.isAssignableFrom(checkClass)) {
                plugin.logger.error(plugin.description.versionedName + " attempted to register an invalid EventHandler method signature \"" + method.toGenericString() + "\" in " + listener.javaClass)
                continue
            }

            val eventClass = checkClass.asSubclass(Event::class.java)
            method.isAccessible = true

            var clazz: Class<*> = eventClass
            while (Event::class.java.isAssignableFrom(clazz)) {
                // This loop checks for extending deprecated events
                if (clazz.getAnnotation<Deprecated?>(Deprecated::class.java) != null) {
                    if (java.lang.String.valueOf(server.settings.baseSettings.deprecatedVerbose).toBoolean()) {
                        log.warn(
                            server.lang.tr(
                                "chorus.plugin.deprecatedEvent",
                                plugin.name,
                                clazz.name,
                                listener.javaClass.name + "." + method.name + "()"
                            )
                        )
                    }
                    break
                }
                clazz = clazz.superclass
            }
            this.registerEvent(
                eventClass,
                listener,
                eh.priority,
                MethodEventExecutor(method),
                plugin,
                eh.ignoreCancelled
            )
        }
    }

    @JvmOverloads
    @Throws(PluginException::class)
    fun registerEvent(
        event: Class<out Event>,
        listener: Listener,
        priority: EventPriority?,
        executor: EventExecutor,
        plugin: Plugin,
        ignoreCancelled: Boolean = false
    ) {
        if (!plugin.isEnabled) {
            throw PluginException("Plugin attempted to register $event while not enabled")
        }

        try {
            getEventListeners(event).register(RegisteredListener(listener, executor, priority, plugin, ignoreCancelled))
        } catch (e: IllegalAccessException) {
            log.error(
                "An error occurred while registering the event listener event:{}, listener:{} for plugin:{} version:{}",
                event, listener, plugin.description.name, plugin.description.version, e
            )
        }
    }

    @Throws(IllegalAccessException::class)
    private fun getEventListeners(type: Class<out Event>): HandlerList {
        try {
            val method = getRegistrationClass(type.kotlin).memberProperties.find { it.name == "handlers" }?.getter!!

            method.isAccessible = true
            return method.call(type.kotlin.companionObjectInstance) as HandlerList
        } catch (e: NullPointerException) {
            throw IllegalArgumentException("getHandlers method in " + type.name + " was not static!", e)
        } catch (e: Exception) {
            val illegalAccessException = IllegalAccessException(Utils.getExceptionMessage(e))
            illegalAccessException.addSuppressed(e)
            throw illegalAccessException
        }
    }

    @Throws(IllegalAccessException::class)
    private fun getRegistrationClass(clazz: KClass<out Event>): KClass<*> {
        val companion = clazz.companionObject
        if (companion != null) {
            val handlersProp = companion.memberProperties.find { it.name == "handlers" }
            if (handlersProp != null) {
                return companion
            }
        }

        throw IllegalAccessException("Unable to find handler list for event " + clazz.jvmName + ". Static getHandlers method required!")
    }

    companion object : Loggable
}
