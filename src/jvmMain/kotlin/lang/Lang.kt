package org.chorus_oss.chorus.lang

import kotlinx.coroutines.runBlocking
import kotlinx.io.buffered
import kotlinx.io.files.FileNotFoundException
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import kotlinx.serialization.json.Json
import org.chorus_oss.chorus.generated.resources.Res
import org.chorus_oss.chorus.utils.Loggable


class Lang @JvmOverloads constructor(lang: String, path: String? = null, fallback: String = FALLBACK_LANGUAGE) :
    Loggable {
    /**
     * The Lang name.
     */
    val langName: String

    /**
     * 本地语言，从nukkit.yml中指定
     */
    var langMap: Map<String, String> = mapOf()
        private set

    /**
     * 备选语言映射，当从本地语言映射中无法翻译时调用备选语言映射，默认为英文
     */
    var fallbackLangMap: Map<String, String> = mapOf()
        private set

    //用于提取字符串中%后带有[a-zA-Z0-9_.-]这些字符的字符串的模式
    private val split: Regex = Regex("%[A-Za-z0-9_.-]+")


    init {
        var path = path
        this.langName = lang.lowercase()
        val useFallback = lang != fallback

        if (path == null) {
            path = "files/language/"
            runBlocking {
                langMap = loadLang(
                    Res.readBytes("$path$langName/lang.json").decodeToString()
                ) ?: mapOf()
                if (useFallback) fallbackLangMap = loadLang(
                    Res.readBytes("$path$fallback/lang.json").decodeToString()
                ) ?: mapOf()
            }
        } else {
            this.langMap = this.loadLangFile(
                Path(path, this.langName + "/lang.json")
            ) ?: mapOf()

            if (useFallback) {
                this.fallbackLangMap = this.loadLangFile(
                    Path("$path$fallback/lang.json")
                ) ?: mapOf()
            }
        }
        if (this.fallbackLangMap.isEmpty()) this.fallbackLangMap = this.langMap
    }

    val name: String
        get() = this["language.name"]

    private fun loadLangFile(path: Path): Map<String, String>? {
        try {
            if (!SystemFileSystem.exists(path) || SystemFileSystem.metadataOrNull(path)!!.isDirectory) {
                throw FileNotFoundException()
            }

            val str = SystemFileSystem.source(path).buffered().readString()

            return parseLang(str)
        } catch (e: Throwable) {
            log.error("Failed to load language at {}", path, e)
            return null
        }
    }

    private fun loadLang(str: String): Map<String, String>? {
        try {
            return parseLang(str)
        } catch (e: Throwable) {
            log.error("Failed to parse the language input stream", e)
            return null
        }
    }

    private fun parseLang(str: String): Map<String, String> {
        return Json.decodeFromString(str)
    }

    /**
     * 翻译一个文本key，key从语言文件中查询
     *
     * @param key the key
     * @return the string
     */
    fun tr(key: String): String {
        return tr(key, *emptyArray())
    }

    /**
     * 翻译一个文本key，key从语言文件中查询，并且按照给定参数填充结果
     *
     * @param key  the key
     * @param args the args
     * @return the string
     */
    fun tr(key: String, vararg args: String): String {
        var baseText = parseLanguageText(key)
        for (i in args.indices) {
            baseText = baseText.replace("{%$i}", parseLanguageText(args[i].toString()))
        }
        return baseText
    }

    /**
     * 翻译一个文本key，key从语言文件中查询，并且按照给定参数填充结果
     *
     *
     * Translate a text key, the key is queried from the language file and the result is populated according to the given parameters
     *
     * @param key  the key
     * @param args the args
     * @return the string
     */
    fun tr(key: String, vararg args: Any): String {
        var baseText = parseLanguageText(key)
        for (i in args.indices) {
            baseText = baseText.replace("{%$i}", parseLanguageText(parseArg(args[i])))
        }
        return baseText
    }

    fun tr(c: TextContainer): String {
        var baseText = this.parseLanguageText(c.text)
        if (c is TranslationContainer) {
            for (i in c.parameters.indices) {
                baseText = baseText.replace("{%$i}", this.parseLanguageText(c.parameters[i]))
            }
        }
        return baseText
    }

    /**
     * 翻译一个文本key，key从语言文件中查询，并且按照给定参数填充结果
     *
     *
     * Translate a text key, the key is queried from the language file and the result is populated according to the given parameters
     *
     * @param str    the str
     * @param params the params
     * @param prefix str的前缀<br></br>Prefix of str
     * @param mode   为true，则只翻译以指定前缀的多语言文本，为false则只翻译不带有指定前缀的多语言文本<br></br>If true translate only multilingual text with the specified prefix, false translate only multilingual text without the specified prefix
     * @return the string
     */
    fun tr(str: String, params: Array<String>, prefix: String, mode: Boolean): String {
        var baseText = parseLanguageText(str, prefix, mode)
        for (i in params.indices) {
            baseText = baseText.replace("{%$i}", parseLanguageText(parseArg(params[i]), prefix, mode))
        }
        return baseText
    }

    /**
     * 获取指定id对应的多语言文本，若不存在则返回null
     *
     * @param id the id
     * @return the string
     */
    fun internalGet(id: String): String? {
        return langMap[id] ?: fallbackLangMap[id]
    }

    /**
     * 获取指定id对应的多语言文本，若不存在则返回id本身
     *
     * @param id the id
     * @return the string
     */
    operator fun get(id: String): String {
        return internalGet(id) ?: id
    }

    private fun parseArg(arg: Any): String {
        return when (arg) {
            is IntArray -> arg.contentToString()
            is DoubleArray -> arg.contentToString()
            is FloatArray -> arg.contentToString()
            is ShortArray -> arg.contentToString()
            is ByteArray -> arg.contentToString()
            is LongArray -> arg.contentToString()
            is BooleanArray -> arg.contentToString()
            else -> arg.toString()
        }
    }

    private fun parseLanguageText(str: String): String {
        return internalGet(str) ?: split.replace(str) {
            this[it.value.substring(1)]
        }
    }

    private fun parseLanguageText(str: String, prefix: String, mode: Boolean): String {
        if (mode && !str.startsWith(prefix)) {
            return str
        }
        if (!mode && str.startsWith(prefix)) {
            return str
        }
        return internalGet(str)
            ?: split.replace(str) {
                val s = it.value.substring(1)
                if (mode) {
                    if (s.startsWith(prefix)) {
                        this[s]
                    } else s
                } else {
                    if (!s.startsWith(prefix)) {
                        this[s]
                    } else s
                }
            }
    }

    companion object {
        /**
         * 默认备选语言，对应language文件夹
         */
        const val FALLBACK_LANGUAGE: String = "eng"
    }
}
