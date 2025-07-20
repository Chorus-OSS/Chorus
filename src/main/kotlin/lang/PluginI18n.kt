package org.chorus_oss.chorus.lang

import com.google.gson.reflect.TypeToken
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.plugin.PluginBase
import org.chorus_oss.chorus.utils.JSONUtils
import org.chorus_oss.chorus.utils.Loggable
import java.io.*
import java.nio.charset.StandardCharsets
import java.util.regex.MatchResult
import java.util.regex.Pattern


class PluginI18n(private val plugin: PluginBase) {
    /**
     * Gets fallback language.
     *
     * @return the fallback language
     */
    /**
     * Sets fallback language.
     *
     * @param fallback the fallback
     */
    /**
     * 插件多语言的默认备选语言
     */
    var fallbackLanguage: LangCode = LangCode.en_US
    private val split: Pattern = Pattern.compile("%[A-Za-z0-9_.-]+")
    private val MULTI_LANGUAGE: MutableMap<LangCode?, MutableMap<String, String>> =
        HashMap()

    /**
     * 翻译一个文本key，key从语言文件中查询
     *
     *
     * Translate a text key, the key is queried from the language file
     *
     * @param lang 要翻译的语言
     * @param key  the key
     * @return the string
     */
    fun tr(lang: LangCode?, key: String): String {
        return tr(lang, key, *emptyArray())
    }

    /**
     * 翻译一个文本key，key从语言文件中查询，并且按照给定参数填充其中参数
     *
     *
     * Translate a text key, the key is queried from the language file and the parameters are filled according to the given parameters
     *
     * @param lang 要翻译的语言
     * @param key  the key
     * @param args the args
     * @return the string
     */
    fun tr(lang: LangCode?, key: String, vararg args: String): String {
        var baseText = parseLanguageText(lang, key)
        for (i in args.indices) {
            baseText = baseText.replace("{%$i}", parseLanguageText(lang, args[i].toString()))
        }
        return baseText
    }

    /**
     * 翻译一个文本key，key从语言文件中查询，并且按照给定参数填充其中参数
     *
     *
     * Translate a text key, the key is queried from the language file and the parameters are filled according to the given parameters
     *
     * @param lang 要翻译的语言
     * @param key  the key
     * @param args the args
     * @return the string
     */
    fun tr(lang: LangCode?, key: String, vararg args: Any): String {
        var baseText = parseLanguageText(lang, key)
        for (i in args.indices) {
            baseText = baseText.replace("{%$i}", parseLanguageText(lang, parseArg(args[i])))
        }
        return baseText
    }

    /**
     * 翻译文本容器
     *
     *
     * Tr string.
     *
     * @param lang 要翻译的语言
     * @param c    the c
     * @return the string
     */
    fun tr(lang: LangCode?, c: TextContainer): String {
        var baseText = this.parseLanguageText(lang, c.text)
        if (c is TranslationContainer) {
            for (i in c.parameters.indices) {
                baseText = baseText.replace("{%$i}", this.parseLanguageText(lang, c.parameters[i]))
            }
        }
        return baseText
    }

    /**
     * 获取指定id对应的多语言文本，若不存在则返回null
     *
     *
     * Get the multilingual text corresponding to the specified id, or return null if it does not exist
     *
     * @param id the id
     * @return the string
     */
    fun get(lang: LangCode?, id: String): String? {
        val map = MULTI_LANGUAGE[lang]
        val fallbackMap = MULTI_LANGUAGE[fallbackLanguage]
        return if (map?.containsKey(id) == true) {
            map[id]
        } else if (fallbackMap?.containsKey(id) == true) {
            fallbackMap[id]
        } else {
            Server.instance.lang.internalGet(id)
        }
    }

    /**
     * 获取指定id对应的多语言文本，若不存在则返回id本身
     *
     *
     * Get the multilingual text corresponding to the specified id, or return the id itself if it does not exist
     *
     * @param id the id
     * @return the string
     */
    fun getOrOriginal(lang: LangCode?, id: String): String? {
        val map: Map<String, String> = MULTI_LANGUAGE[lang]!!
        val fallbackMap: Map<String, String>
        return if (map.containsKey(id)) {
            map[id]
        } else if ((MULTI_LANGUAGE[fallbackLanguage].also {
                fallbackMap = it!!
            })!!.containsKey(id)) {
            fallbackMap[id]
        } else {
            Server.instance.lang[id]
        }
    }

    protected fun parseLanguageText(lang: LangCode?, str: String): String {
        val result = get(lang, str)
        if (result != null) {
            return result
        } else {
            val matcher = split.matcher(str)
            return matcher.replaceAll { m: MatchResult ->
                this.getOrOriginal(
                    lang,
                    m.group().substring(1)
                )
            }
        }
    }

    /**
     * Add lang.
     *
     * @param langName the lang name
     * @param path     the path
     */
    fun addLang(langName: LangCode?, path: String) {
        try {
            val file = File(path)
            if (!file.exists()) {
                throw FileNotFoundException()
            }
            check(file.name.endsWith(".json"))
            FileInputStream(file).use { stream ->
                MULTI_LANGUAGE.put(
                    langName,
                    parseLang(BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8)))
                )
            }
        } catch (e: IOException) {
            log.error("Failed to load language at {}", path, e)
        }
    }

    /**
     * Add lang.
     *
     * @param langName the lang name
     * @param stream   the stream
     */
    fun addLang(langName: LangCode?, stream: InputStream) {
        try {
            MULTI_LANGUAGE[langName] =
                parseLang(BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8)))
        } catch (e: IOException) {
            log.error("Failed to parse the language input stream", e)
        }
    }

    /**
     * Reload all lang for the i18n.
     *
     * @return whether reload success
     */
    fun reloadLangAll(): Boolean {
        return PluginI18nManager.reload(plugin)
    }

    /**
     * Reload all lang for the i18n from the path folder.
     *
     * @param path the folder
     * @return whether reload success
     */
    fun reloadLangAll(path: String): Boolean {
        return PluginI18nManager.reload(plugin, path)
    }

    /**
     * Reload lang boolean.
     *
     * @param lang the lang
     * @param path the path
     * @return the boolean
     */
    fun reloadLang(lang: LangCode?, path: String): Boolean {
        try {
            val file = File(path)
            if (!file.exists() || file.isDirectory) {
                throw FileNotFoundException()
            }
            FileInputStream(file).use { stream ->
                return reloadLang(lang, BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8)))
            }
        } catch (e: IOException) {
            log.error("Failed to load language at {}", path, e)
            return false
        }
    }

    /**
     * Reload lang boolean.
     *
     * @param lang   the lang
     * @param stream the stream
     * @return the boolean
     */
    fun reloadLang(lang: LangCode?, stream: InputStream): Boolean {
        return reloadLang(lang, BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8)))
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as PluginI18n
        return plugin == that.plugin
    }

    override fun hashCode(): Int {
        return plugin.hashCode()
    }

    protected fun parseArg(arg: Any): String {
        return when (arg.javaClass.simpleName) {
            "int[]" -> {
                (arg as IntArray).contentToString()
            }

            "double[]" -> {
                (arg as DoubleArray).contentToString()
            }

            "float[]" -> {
                (arg as FloatArray).contentToString()
            }

            "short[]" -> {
                (arg as ShortArray).contentToString()
            }

            "byte[]" -> {
                (arg as ByteArray).contentToString()
            }

            "long[]" -> {
                (arg as LongArray).contentToString()
            }

            "boolean[]" -> {
                (arg as BooleanArray).contentToString()
            }

            else -> {
                arg.toString()
            }
        }
    }

    private fun reloadLang(lang: LangCode?, reader: BufferedReader): Boolean {
        val d = MULTI_LANGUAGE[lang]
        val map = Json.parseToJsonElement(reader.readText()).jsonObject.entries.associate {
            it.key to it.value.jsonPrimitive.content
        }.toMutableMap()
        if (d == null) {
            MULTI_LANGUAGE[lang] = map
        } else {
            d.clear()
            d.putAll(map)
        }
        return true
    }

    @Throws(IOException::class)
    private fun parseLang(reader: BufferedReader): MutableMap<String, String> {
        return Json.parseToJsonElement(reader.readText()).jsonObject.entries.associate {
            it.key to it.value.jsonPrimitive.content
        }.toMutableMap()
    }

    companion object : Loggable
}

