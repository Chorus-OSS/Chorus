package org.chorus_oss.chorus.lang

import org.chorus_oss.chorus.plugin.PluginBase
import org.chorus_oss.chorus.utils.Loggable
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.jar.JarFile

/**
 * 注册插件多语言，要求插件资源文件中存在一个language文件夹，或者指定language文件夹的外部保存路径
 *
 *
 * 多语言文件要求以[LangCode].lang的格式保存
 *
 *
 * To register a plugin for multiple languages, require the existence of a language folder in the plugin resource file, or specify an external path to the language folder
 *
 *
 * Multi-language files are required to be saved in the format [LangCode].lang
 *
 *
 * Only support Java Plugin [PluginBase]
 */

object PluginI18nManager : Loggable {
    private val PLUGINS_MULTI_LANGUAGE = HashMap<String, PluginI18n>()

    /**
     * 重新加载指定插件的多语言，多语言保存在插件jar中的language文件夹下
     *
     *
     * Reload the multilanguage of the specified plugin, which is stored in the language folder of the plugin jar
     *
     * @param plugin the plugin
     * @return the boolean
     */
    fun reload(plugin: PluginBase): Boolean {
        val i18n = PLUGINS_MULTI_LANGUAGE[plugin.file.name] ?: return false
        try {
            JarFile(plugin.file).use { jarFile ->
                val jarEntrys = jarFile.entries()
                var count = 0
                while (jarEntrys.hasMoreElements()) {
                    val entry = jarEntrys.nextElement()
                    val name = entry.name
                    if (name.startsWith("language") && name.endsWith(".json")) {
                        // 开始读取文件内容
                        val inputStream = checkNotNull(plugin.getResource(name))
                        i18n.reloadLang(LangCode.Companion.from(name.substring(9, name.indexOf("."))), inputStream)
                        count++
                        inputStream.close()
                    }
                }
                return count > 0
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    /**
     * 重新加载指定插件的多语言
     *
     *
     * Reload multilingual for a given plugin
     *
     * @param plugin the plugin
     * @param path   language文件夹的路径
     * @return the boolean
     */
    fun reload(plugin: PluginBase, path: String): Boolean {
        val i18n = PLUGINS_MULTI_LANGUAGE[plugin.file.name] ?: return false
        val file = File(path)
        if (file.exists() && file.isDirectory) {
            val files = checkNotNull(file.listFiles())
            var count = 0
            for (f in files) {
                try {
                    FileInputStream(f).use { inputStream ->
                        i18n.reloadLang(LangCode.Companion.from(f.name.replace(".json", "")), inputStream)
                        count++
                    }
                } catch (e: IOException) {
                    throw RuntimeException(e)
                }
            }
            return count > 0
        } else {
            log.error("The path does not represent a folder!")
            return false
        }
    }

    /**
     * 注册插件多语言
     *
     *
     * Register Plugin Multilanguage
     *
     * @param plugin the plugin
     * @return the boolean
     */
    fun register(plugin: PluginBase): PluginI18n {
        try {
            JarFile(plugin.file).use { jarFile ->
                val jarEntrys = jarFile.entries()
                val pluginMultiLanguage = PluginI18n(plugin)
                while (jarEntrys.hasMoreElements()) {
                    val entry = jarEntrys.nextElement()
                    val name = entry.name
                    if (name.startsWith("language") && name.endsWith(".json")) {
                        // 开始读取文件内容
                        val inputStream = checkNotNull(plugin.getResource(name))
                        pluginMultiLanguage.addLang(
                            LangCode.Companion.from(name.substring(9, name.indexOf("."))),
                            inputStream
                        )
                        inputStream.close()
                    }
                }
                PLUGINS_MULTI_LANGUAGE[plugin.file.name] = pluginMultiLanguage
                return pluginMultiLanguage
            }
        } catch (e: IOException) {
            throw RuntimeException("No language exists in the plugin resources folder")
        }
    }

    /**
     * 注册插件多语言
     *
     *
     * Register Plugin Multilanguage
     *
     * @param plugin the plugin
     * @param path   language文件夹的路径<br></br>Path to the language folder
     * @return the boolean
     */
    fun register(plugin: PluginBase, path: String): PluginI18n {
        val file = File(path)
        if (file.exists() && file.isDirectory) {
            val files = checkNotNull(file.listFiles())
            val pluginMultiLanguage = PluginI18n(plugin)
            for (f in files) {
                try {
                    FileInputStream(f).use { inputStream ->
                        pluginMultiLanguage.addLang(LangCode.Companion.from(f.name.replace(".json", "")), inputStream)
                    }
                } catch (e: IOException) {
                    throw RuntimeException(e)
                }
            }
            PLUGINS_MULTI_LANGUAGE[plugin.file.name] = pluginMultiLanguage
            return pluginMultiLanguage
        } else {
            throw RuntimeException("The path does not represent a folder or not exists!")
        }
    }

    fun getI18n(plugin: PluginBase): PluginI18n? {
        return PLUGINS_MULTI_LANGUAGE[plugin.file.name]
    }
}
