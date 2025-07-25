package org.chorus_oss.chorus.plugin

import java.io.File
import java.util.regex.Pattern

/**
 * 描述一个插件加载器的接口。<br></br>
 * An interface to describe a plugin loader.
 *
 * @see JavaPluginLoader
 */
interface PluginLoader {
    /**
     * 通过文件名字的字符串，来加载和初始化一个插件。<br></br>
     * Loads and initializes a plugin by its file name.
     *
     *
     * 这个方法应该设置好插件的相关属性。比如，插件所在的服务器对象，插件的加载器对象，插件的描述对象，插件的数据文件夹。<br></br>
     * Properties for loaded plugin should be set in this method. Such as, the `Server` object for which this
     * plugin is running in, the `PluginLoader` object for its loader, and the `File` object for its
     * data folder.
     *
     *
     * 如果插件加载失败，这个方法应该返回`null`，或者抛出异常。<br></br>
     * If the plugin loader does not load this plugin successfully, a `null` should be returned,
     * or an exception should be thrown.
     *
     * @param filename 这个插件的文件名字字符串。<br></br>A string of its file name.
     * @return 加载完毕的插件的 `Plugin`对象。<br></br>The loaded plugin as a `Plugin` object.
     * @throws java.lang.Exception 插件加载失败所抛出的异常。<br></br>Thrown when an error occurred.
     * @see .loadPlugin
     * @see org.chorus_oss.chorus.plugin.PluginBase.init
     *
     */
    @Throws(Exception::class)
    fun loadPlugin(filename: String): Plugin?

    /**
     * 通过插件的 `File`对象，来加载和初始化一个插件。<br></br>
     * Loads and initializes a plugin by a `File` object describes the file.
     *
     *
     * 这个方法应该设置好插件的相关属性。比如，插件所在的服务器对象，插件的加载器对象，插件的描述对象，插件的数据文件夹。<br></br>
     * Properties for loaded plugin should be set in this method. Such as, the `Server` object for which this
     * plugin is running in, the `PluginLoader` object for its loader, and the `File` object for its
     * data folder.
     *
     *
     * 如果插件加载失败，这个方法应该返回`null`，或者抛出异常。<br></br>
     * If the plugin loader does not load this plugin successfully, a `null` should be returned,
     * or an exception should be thrown.
     *
     * @param file 这个插件的文件的 `File`对象。<br></br>A `File` object for this plugin.
     * @return 加载完毕的插件的 `Plugin`对象。<br></br>The loaded plugin as a `Plugin` object.
     * @throws java.lang.Exception 插件加载失败所抛出的异常。<br></br>Thrown when an error occurred.
     * @see .loadPlugin
     * @see org.chorus_oss.chorus.plugin.PluginBase.init
     *
     */
    @Throws(Exception::class)
    fun loadPlugin(file: File): Plugin?

    /**
     * 通过插件文件名的字符串，来获得描述这个插件的 `PluginDescription`对象。<br></br>
     * Gets a `PluginDescription` object describes the plugin by its file name.
     *
     *
     * 如果插件的描述对象获取失败，这个方法应该返回`null`。<br></br>
     * If the plugin loader does not get its description successfully, a `null` should be returned.
     *
     * @param filename 这个插件的文件名字。<br></br>A string of its file name.
     * @return 描述这个插件的 `PluginDescription`对象。<br></br>
     * A `PluginDescription` object describes the plugin.
     * @see .getPluginDescription
     *
     */
    fun getPluginDescription(filename: String): PluginTOML?

    /**
     * 通过插件的 `File`对象，来获得描述这个插件的 `PluginDescription`对象。<br></br>
     * Gets a `PluginDescription` object describes the plugin by a `File` object describes the plugin file.
     *
     *
     * 如果插件的描述对象获取失败，这个方法应该返回`null`。<br></br>
     * If the plugin loader does not get its description successfully, a `null` should be returned.
     *
     * @param file 这个插件的文件的 `File`对象。<br></br>A `File` object for this plugin.
     * @return 描述这个插件的 `PluginDescription`对象。<br></br>
     * A `PluginDescription` object describes the plugin.
     * @see .getPluginDescription
     *
     */
    fun getPluginDescription(file: File): PluginTOML?

    /**
     * 返回这个插件加载器支持的文件类型。<br></br>
     * Returns the file types this plugin loader supports.
     *
     *
     * 在Nukkit读取所有插件时，插件管理器会查找所有已经安装的插件加载器，通过识别这个插件是否满足下面的条件，
     * 来选择对应的插件加载器。<br></br>
     * When Nukkit is trying to load all its plugins, the plugin manager will look for all installed plugin loader,
     * and choose the correct one by checking if this plugin matches the filters given below.
     *
     *
     * 举个例子，识别这个文件是否以jar为扩展名，它的正则表达式是：<br></br>
     * For example, to check if this file is has a "jar" extension, the regular expression should be:<br></br>
     * `^.+\\.jar$`<br></br>
     * 所以只读取jar扩展名的插件加载器，这个函数应该写成：<br></br>
     * So, for a jar-extension-only file plugin loader, this method should be:
     *
     * <pre> `@Override`
     * public Pattern[] getPluginFilters() {
     * return new Pattern[]{Pattern.compile("^.+\\.jar$")};
     * }
    </pre> *
     *
     * @return 表达这个插件加载器支持的文件类型的正则表达式数组。<br></br>
     * An array of regular expressions, that describes what kind of file this plugin loader supports.
     *
     */
    val pluginFilters: Array<Pattern>

    /**
     * 启用一个插件。<br></br>
     * Enables a plugin.
     *
     * @param plugin 要被启用的插件。<br></br>The plugin to enable.
     * @see .disablePlugin
     *
     *
     */
    fun enablePlugin(plugin: Plugin)

    /**
     * 停用一个插件。<br></br>
     * Disables a plugin.
     *
     * @param plugin 要被停用的插件。<br></br>The plugin to disable.
     * @see .enablePlugin
     *
     *
     */
    fun disablePlugin(plugin: Plugin)
}
