package org.chorus_oss.chorus.plugin

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.command.Command
import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.PluginCommand
import org.chorus_oss.chorus.command.PluginIdentifiableCommand
import org.chorus_oss.chorus.utils.Config
import org.chorus_oss.chorus.utils.ConfigSection
import org.chorus_oss.chorus.utils.Loggable
import org.chorus_oss.chorus.utils.Utils
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.IOException
import java.io.InputStream

/**
 * 一般的Nukkit插件需要继承的类。<br></br>
 * A class to be extended by a normal Nukkit plugin.
 *
 * @see org.chorus_oss.chorus.plugin.PluginTOML
 */

abstract class PluginBase : Plugin {
    override lateinit var pluginLoader: PluginLoader
        protected set
    override lateinit var pluginClassLoader: ClassLoader
        protected set
    override lateinit var server: Server
        protected set
    override var isEnabled: Boolean = false
        set(value) {
            if (field != value) {
                if (!value && InternalPlugin.INSTANCE == this) {
                    throw UnsupportedOperationException("The Chorus Internal Plugin cannot be disabled")
                }
                field = value
                if (isEnabled) {
                    onEnable()
                } else {
                    onDisable()
                }
            }
        }

    /**
     * 返回这个插件是否已经初始化。<br></br>
     * Returns if this plugin is initialized.
     *
     * @return 这个插件是否已初始化。<br></br>if this plugin is initialized.
     *
     */
    var isInitialized: Boolean = false
        private set
    override lateinit var description: PluginTOML
        protected set
    override var dataFolder: File? = null
        protected set
    override var config: Config? = null
        get() {
            if (field == null) {
                this.reloadConfig()
            }
            return field
        }

    private lateinit var configFile: File

    /**
     * 返回这个插件的文件`File`对象。对于jar格式的插件，就是jar文件本身。<br></br>
     * Returns the `File` object of this plugin itself. For jar-packed plugins, it is the jar file itself.
     *
     * @return 这个插件的文件 `File`对象。<br></br>The `File` object of this plugin itself.
     *
     */
    override lateinit var file: File
        protected set
    override lateinit var logger: PluginLogger
        protected set


    override fun onLoad() {
    }

    override fun onEnable() {
    }

    override fun onDisable() {
    }

    override val isDisabled: Boolean
        get() = !isEnabled

    /**
     * 初始化这个插件。<br></br>
     * Initialize the plugin.
     *
     *
     *
     * 这个方法会在加载(load)之前被插件加载器调用，初始化关于插件的一些事项，不能被重写。<br></br>
     * Called by plugin loader before load, and initialize the plugin. Can't be overridden.
     *
     * @param loader      加载这个插件的插件加载器的`PluginLoader`对象。<br></br>
     * The plugin loader ,which loads this plugin, as a `PluginLoader` object.
     * @param server      运行这个插件的服务器的`Server`对象。<br></br>
     * The server running this plugin, as a `Server` object.
     * @param description 描述这个插件的`PluginDescription`对象。<br></br>
     * A `PluginDescription` object that describes this plugin.
     * @param dataFolder  这个插件的数据的文件夹。<br></br>
     * The data folder of this plugin.
     * @param file        这个插件的文件`File`对象。对于jar格式的插件，就是jar文件本身。<br></br>
     * The `File` object of this plugin itself. For jar-packed plugins, it is the jar file itself.
     *
     */
    fun init(
        loader: PluginLoader,
        classLoader: ClassLoader,
        server: Server,
        description: PluginTOML,
        dataFolder: File,
        file: File
    ) {
        if (!isInitialized) {
            isInitialized = true
            this.pluginLoader = loader
            this.pluginClassLoader = classLoader
            this.server = server
            this.description = description
            this.dataFolder = dataFolder
            this.file = file
            this.configFile = File(this.dataFolder, "config.yml")
            this.logger = PluginLogger(this)
        }
    }

    /**
     * TODO: FINISH JAVADOC
     */
    fun getCommand(name: String): PluginIdentifiableCommand? {
        var command = Server.instance.getPluginCommand(name)
        if (command == null || !command.plugin.equals(this)) {
            command = Server.instance.getPluginCommand(description.name.lowercase() + ":" + name)
        }

        return if (command != null && command.plugin.equals(this)) {
            command
        } else {
            null
        }
    }

    fun getPluginCommand(name: String): PluginCommand<*>? {
        val command = getCommand(name)
        if (command is PluginCommand<*>) {
            return command
        }
        return null
    }

    override fun onCommand(sender: CommandSender?, command: Command?, label: String?, args: Array<String?>?): Boolean {
        return false
    }

    override fun getResource(filename: String): InputStream {
        try {
            return javaClass.module.getResourceAsStream(filename)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    override fun saveResource(filename: String): Boolean {
        return saveResource(filename, false)
    }

    override fun saveResource(filename: String, replace: Boolean): Boolean {
        return saveResource(filename, filename, replace)
    }

    override fun saveResource(filename: String, outputName: String, replace: Boolean): Boolean {
        check(
            !filename.trim { it <= ' ' }.isEmpty() && !outputName.trim { it <= ' ' }.isEmpty()
        ) { "Filename can not be empty!" }

        val out = File(dataFolder, outputName)
        if (!out.exists() || replace) {
            try {
                getResource(filename).use { resource ->
                    val outFolder = out.parentFile
                    if (!outFolder.exists()) {
                        outFolder.mkdirs()
                    }
                    Utils.writeFile(out, resource)

                    return true
                }
            } catch (e: IOException) {
                log.error(
                    "Error while saving resource {}, to {} (replace: {}, plugin:{})",
                    filename,
                    outputName,
                    replace,
                    description.name,
                    e
                )
            }
        }
        return false
    }

    override fun saveConfig() {
        if (!config!!.save()) {
            logger.critical("Could not save config to " + configFile.toString())
        }
    }

    override fun saveDefaultConfig() {
        if (!configFile.exists()) {
            this.saveResource("config.yml", false)
        }
    }

    override fun reloadConfig() {
        this.config = Config(this.configFile)
        try {
            getResource("config.yml").use { configStream ->
                val dumperOptions = DumperOptions()
                dumperOptions.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
                val yaml = Yaml(dumperOptions)
                try {
                    config!!.setDefault(
                        yaml.loadAs(
                            Utils.readFile(this.configFile),
                            ConfigSection::class.java
                        )
                    )
                } catch (e: IOException) {
                    log.error(
                        "Error while reloading configs for the plugin {}",
                        description.name,
                        e
                    )
                }
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    override val name: String
        get() = description.name

    val fullName: String
        /**
         * 返回这个插件完整的名字。<br></br>
         * Returns the full name of this plugin.
         *
         *
         *
         * 一个插件完整的名字由`名字+" v"+版本号`组成。比如：<br></br>
         * A full name of a plugin is composed by `name+" v"+version`.for example:
         *
         * `HelloWorld v1.0.0`
         *
         * @return 这个插件完整的名字。<br></br>The full name of this plugin.
         * @see org.chorus_oss.chorus.plugin.PluginTOML.getFullName
         *
         *
         */
        get() = description.versionedName

    companion object : Loggable
}
