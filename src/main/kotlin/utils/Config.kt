package org.chorus_oss.chorus.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.plugin.InternalPlugin
import org.chorus_oss.chorus.scheduler.FileWriteTask
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

class Config {
    var rootSection: ConfigSection = ConfigSection()
        private set

    private var file: File? = null

    var isCorrect: Boolean = false
        private set

    private var type = DETECT

    /**
     * Constructor for Config instance with undefined file object
     *
     * @param type - Config type
     */
    @JvmOverloads
    constructor(type: Int = YAML) {
        this.type = type
        this.isCorrect = true
        this.rootSection = ConfigSection()
    }

    constructor(file: File) : this(file.toString(), DETECT)

    constructor(file: File, type: Int) : this(file.toString(), type, ConfigSection())

    @JvmOverloads
    constructor(file: String, type: Int = DETECT, defaultMap: ConfigSection = ConfigSection()) {
        this.load(file, type, defaultMap)
    }

    constructor(file: File, type: Int, defaultMap: ConfigSection) {
        this.load(file.toString(), type, defaultMap)
    }

    fun reload() {
        rootSection.clear()
        this.isCorrect = false
        checkNotNull(this.file) { "Failed to reload Config. File object is undefined." }
        this.load(file.toString(), this.type)
    }

    @JvmOverloads
    fun load(file: String, type: Int = DETECT, defaultMap: ConfigSection = ConfigSection()): Boolean {
        this.isCorrect = true
        this.type = type
        this.file = File(file)
        if (!this.file!!.exists()) {
            try {
                this.file!!.parentFile.mkdirs()
                this.file!!.createNewFile()
            } catch (e: IOException) {
                log.error("Could not create Config {}", this.file.toString(), e)
            }
            this.rootSection = defaultMap
            this.save()
        } else {
            if (this.type == DETECT) {
                var extension = ""
                if (this.file!!.name.lastIndexOf(".") != -1 && this.file!!.name.lastIndexOf(".") != 0) {
                    extension = this.file!!.name.substring(this.file!!.name.lastIndexOf(".") + 1)
                }
                if (format.containsKey(extension)) {
                    this.type = format[extension]!!
                } else {
                    this.isCorrect = false
                }
            }
            if (this.isCorrect) {
                var content = ""
                try {
                    content = Utils.readFile(this.file!!)
                } catch (e: IOException) {
                    log.error("An error occurred while loading the file {}", file, e)
                }
                this.parseContent(content)
                if (!this.isCorrect) return false
                if (this.setDefault(defaultMap) > 0) {
                    this.save()
                }
            } else {
                return false
            }
        }
        return true
    }

    fun load(inputStream: InputStream?): Boolean {
        if (inputStream == null) return false
        if (this.isCorrect) {
            val content: String
            try {
                content = Utils.readFile(inputStream)
            } catch (e: IOException) {
                log.error(
                    "An error occurred while loading a config from an input stream, input: {}",
                    inputStream,
                    e
                )
                return false
            }
            this.parseContent(content)
        }
        try {
            inputStream.close()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        return isCorrect
    }

    fun loadAsJson(inputStream: InputStream?, gson: Gson): Boolean {
        if (inputStream == null) return false
        if (this.isCorrect) {
            val content: String
            try {
                content = Utils.readFile(inputStream)
            } catch (e: IOException) {
                log.error(
                    "An error occurred while loading a config from an input stream, input: {}",
                    inputStream,
                    e
                )
                return false
            }
            this.parseContentAsJson(content, gson)
        }
        return isCorrect
    }

    fun check(): Boolean {
        return this.isCorrect
    }

    /**
     * Save configuration into provided file. Internal file object will be set to new file.
     *
     * @param file
     * @param async
     * @return
     */
    fun save(file: File?, async: Boolean): Boolean {
        this.file = file
        return save(async)
    }

    fun save(file: File?): Boolean {
        this.file = file
        return save()
    }

    fun saveAsJson(file: File, async: Boolean, gson: Gson): Boolean {
        this.file = file
        return saveAsJson(async, gson)
    }

    fun saveAsJson(async: Boolean, gson: Gson): Boolean {
        if (!this.isCorrect) {
            return false
        }
        save0(async, StringBuilder(gson.toJson(this.rootSection)).append('\n'))
        return true
    }

    @JvmOverloads
    fun save(async: Boolean = false): Boolean {
        checkNotNull(this.file) { "Failed to save Config. File object is undefined." }
        if (this.isCorrect) {
            var content = StringBuilder()
            when (this.type) {
                PROPERTIES -> content = StringBuilder(this.writeProperties())
                JSON -> content = StringBuilder(
                    GsonBuilder().setPrettyPrinting().create().toJson(
                        this.rootSection
                    )
                )

                YAML -> {
                    val dumperOptions = DumperOptions()
                    dumperOptions.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
                    val yaml = Yaml(dumperOptions)
                    content = StringBuilder(yaml.dump(this.rootSection))
                }

                ENUM -> for (o in rootSection.entries) {
                    val entry = o as Map.Entry<*, *>
                    content.append(entry.key).append("\r\n")
                }
            }
            save0(async, content)
            return true
        } else {
            return false
        }
    }

    private fun save0(async: Boolean, content: StringBuilder) {
        if (async) {
            Server.instance.scheduler.scheduleAsyncTask(
                InternalPlugin.INSTANCE, FileWriteTask(
                    file!!, content.toString()
                )
            )
        } else {
            try {
                Utils.writeFile(file!!, content.toString())
            } catch (e: IOException) {
                log.error("Failed to save the config file {}", file, e)
            }
        }
    }

    operator fun set(key: String, value: Any?) {
        rootSection[key] = value
    }

    operator fun get(key: String?): Any? {
        return this.get<Any?>(key, null)
    }

    fun <T> get(key: String?, defaultValue: T): T? {
        return if (this.isCorrect) rootSection.get(key, defaultValue) else defaultValue
    }

    fun getSection(key: String?): ConfigSection {
        return if (this.isCorrect) rootSection.getSection(key) else ConfigSection()
    }

    fun isSection(key: String?): Boolean {
        return rootSection.isSection(key)
    }

    fun getSections(key: String?): ConfigSection {
        return if (this.isCorrect) rootSection.getSections(key) else ConfigSection()
    }

    val sections: ConfigSection
        get() = if (this.isCorrect) rootSection.sections else ConfigSection()

    fun getInt(key: String?): Int {
        return this.getInt(key, 0)
    }

    fun getInt(key: String?, defaultValue: Int): Int {
        return if (this.isCorrect) rootSection.getInt(key, defaultValue) else defaultValue
    }

    fun isInt(key: String?): Boolean {
        return rootSection.isInt(key)
    }

    fun getLong(key: String?): Long {
        return this.getLong(key, 0)
    }

    fun getLong(key: String?, defaultValue: Long): Long {
        return if (this.isCorrect) rootSection.getLong(key, defaultValue) else defaultValue
    }

    fun isLong(key: String?): Boolean {
        return rootSection.isLong(key)
    }

    fun getDouble(key: String?): Double {
        return this.getDouble(key, 0.0)
    }

    fun getDouble(key: String?, defaultValue: Double): Double {
        return if (this.isCorrect) rootSection.getDouble(key, defaultValue) else defaultValue
    }

    fun isDouble(key: String?): Boolean {
        return rootSection.isDouble(key)
    }

    fun getString(key: String?): String {
        return this.getString(key, "")
    }

    fun getString(key: String?, defaultValue: String): String {
        return if (this.isCorrect) rootSection.getString(key, defaultValue) else defaultValue
    }

    fun isString(key: String?): Boolean {
        return rootSection.isString(key)
    }

    fun getBoolean(key: String?): Boolean {
        return this.getBoolean(key, false)
    }

    fun getBoolean(key: String?, defaultValue: Boolean): Boolean {
        return if (this.isCorrect) rootSection.getBoolean(key, defaultValue) else defaultValue
    }

    fun isBoolean(key: String?): Boolean {
        return rootSection.isBoolean(key)
    }

    fun getList(key: String?): List<*>? {
        return this.getList(key, null)
    }

    fun getList(key: String?, defaultList: List<*>?): List<*>? {
        return if (this.isCorrect) rootSection.getList(key, defaultList) else defaultList
    }

    fun isList(key: String?): Boolean {
        return rootSection.isList(key)
    }

    fun getStringList(key: String?): List<String?> {
        return rootSection.getStringList(key)
    }

    fun getIntegerList(key: String?): List<Int?> {
        return rootSection.getIntegerList(key)
    }

    fun getBooleanList(key: String?): List<Boolean?> {
        return rootSection.getBooleanList(key)
    }

    fun getDoubleList(key: String?): List<Double?> {
        return rootSection.getDoubleList(key)
    }

    fun getFloatList(key: String?): List<Float?> {
        return rootSection.getFloatList(key)
    }

    fun getLongList(key: String?): List<Long?> {
        return rootSection.getLongList(key)
    }

    fun getByteList(key: String?): List<Byte?> {
        return rootSection.getByteList(key)
    }

    fun getCharacterList(key: String?): List<Char?> {
        return rootSection.getCharacterList(key)
    }

    fun getShortList(key: String?): List<Short?> {
        return rootSection.getShortList(key)
    }

    fun getMapList(key: String?): List<Map<*, *>?> {
        return rootSection.getMapList(key)
    }

    fun setAll(map: LinkedHashMap<String?, Any?>?) {
        this.rootSection = ConfigSection(map)
    }

    fun setAll(section: ConfigSection) {
        this.rootSection = section
    }

    fun exists(key: String): Boolean {
        return rootSection.exists(key)
    }

    fun exists(key: String, ignoreCase: Boolean): Boolean {
        return rootSection.exists(key, ignoreCase)
    }

    fun remove(key: String?) {
        rootSection.remove(key)
    }

    val all: Map<String?, Any?>
        get() = rootSection.allMap

    fun setDefault(map: LinkedHashMap<String?, Any?>?): Int {
        return setDefault(ConfigSection(map))
    }

    fun setDefault(map: ConfigSection): Int {
        val size = rootSection.size
        this.rootSection = this.fillDefaults(map, this.rootSection)
        return rootSection.size - size
    }

    private fun fillDefaults(defaultMap: ConfigSection, data: ConfigSection): ConfigSection {
        for (key in defaultMap.keys) {
            if (!data.containsKey(key)) {
                data[key] = defaultMap[key]
            }
        }
        return data
    }

    private fun parseList(content: String) {
        var content1 = content
        content1 = content1.replace("\r\n", "\n")
        for (v in content1.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
            if (v.trim { it <= ' ' }.isEmpty()) {
                continue
            }
            rootSection[v] = true
        }
    }

    private fun writeProperties(): String {
        val content = StringBuilder(
            """
                #Properties Config file
                #${SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(Date())}
                
                """.trimIndent()
        )
        for (o in rootSection.entries) {
            val entry = o as Map.Entry<*, *>
            var v = entry.value
            val k = entry.key!!
            if (v is Boolean) {
                v = if (v) "on" else "off"
            }
            content.append(k).append("=").append(v).append("\r\n")
        }
        return content.toString()
    }

    private fun parseProperties(content: String) {
        for (line in content.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
            if (Pattern.compile("[a-zA-Z0-9\\-_.]*+=+[^\\r\\n]*").matcher(line).matches()) {
                val splitIndex = line.indexOf('=')
                if (splitIndex == -1) {
                    continue
                }
                val key = line.substring(0, splitIndex)
                val value = line.substring(splitIndex + 1)
                val valueLower = value.lowercase()
                if (rootSection.containsKey(key)) {
                    log.debug(
                        "[Config] Repeated property {} on file {}", key,
                        file.toString()
                    )
                }
                when (valueLower) {
                    "on", "true", "yes" -> rootSection[key] = true
                    "off", "false", "no" -> rootSection[key] = false
                    else -> rootSection[key] = value
                }
            }
        }
    }

    private fun parseContentAsJson(content: String, gson: Gson) {
        try {
            this.rootSection =
                ConfigSection(gson.fromJson(content, object : TypeToken<LinkedHashMap<String?, Any?>?>() {
                }.type))
        } catch (e: Exception) {
            log.warn("Failed to parse the config file {}", file, e)
            throw e
        }
    }

    private fun parseContent(content: String) {
        try {
            when (this.type) {
                PROPERTIES -> this.parseProperties(content)
                JSON -> this.rootSection =
                    ConfigSection(JSONUtils.from(content, object : TypeToken<LinkedHashMap<String?, Any?>?>() {}.type))

                YAML -> {
                    val dumperOptions = DumperOptions()
                    dumperOptions.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
                    val yaml = Yaml(dumperOptions)
                    this.rootSection = ConfigSection(
                        yaml.loadAs(
                            content,
                            LinkedHashMap::class.java
                        )
                    )
                }

                ENUM -> this.parseList(content)
                else -> this.isCorrect = false
            }
        } catch (e: Exception) {
            log.warn("Failed to parse the config file {}", file, e)
            throw e
        }
    }

    fun getKeys(): Set<String?> {
        if (this.isCorrect) return rootSection.keys
        return HashSet()
    }

    fun getKeys(child: Boolean): Set<String?> {
        if (this.isCorrect) return rootSection.getKeys(child)
        return HashSet()
    }

    companion object : Loggable {

        const val DETECT: Int = -1 //Detect by file extension
        const val PROPERTIES: Int = 0 // .properties
        const val CNF: Int = PROPERTIES // .cnf
        const val JSON: Int = 1 // .js, .json
        const val YAML: Int = 2 // .yml, .yaml

        //public static final int EXPORT = 3; // .export, .xport
        //public static final int SERIALIZED = 4; // .sl
        const val ENUM: Int = 5 // .txt, .list, .enum
        const val ENUMERATION: Int = ENUM

        val format: MutableMap<String, Int> = TreeMap()

        init {
            format["properties"] = PROPERTIES
            format["con"] = PROPERTIES
            format["conf"] = PROPERTIES
            format["config"] = PROPERTIES
            format["js"] = JSON
            format["json"] = JSON
            format["yml"] = YAML
            format["yaml"] = YAML
            //format.put("sl", Config.SERIALIZED);
            //format.put("serialize", Config.SERIALIZED);
            format["txt"] = ENUM
            format["list"] = ENUM
            format["enum"] = ENUM
        }
    }
}
