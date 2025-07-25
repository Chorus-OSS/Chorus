package org.chorus_oss.chorus.utils

import java.util.function.Consumer

class ConfigSection() : LinkedHashMap<String?, Any?>() {
    /**
     * Constructor of ConfigSection that contains initial key/value data
     *
     * @param key
     * @param value
     */
    constructor(key: String, value: Any?) : this() {
        this.set(key, value)
    }

    /**
     * Constructor of ConfigSection, based on values stored in map.
     *
     * @param map
     */
    constructor(map: LinkedHashMap<String?, Any?>?) : this() {
        if (map.isNullOrEmpty()) return
        for ((key, value) in map) {
            when (value) {
                is LinkedHashMap<*, *> -> {
                    val safeMap = value.entries
                        .filter { it.key is String? }
                        .associate { it.key as String? to it.value }
                        .let { LinkedHashMap(it) }
                    super.put(key, ConfigSection(safeMap))
                }

                is Map<*, *> -> {
                    val safeMap = value.entries
                        .filter { it.key is String? }
                        .associate { it.key as String? to it.value }
                    super.put(key, ConfigSection(safeMap))
                }

                is List<*> -> super.put(key, parseList(value))
                else -> super.put(key, value)
            }
        }
    }

    constructor(map: Map<String?, Any?>?) : this() {
        if (map.isNullOrEmpty()) return
        for ((key, value) in map) {
            when (value) {
                is LinkedHashMap<*, *> -> {
                    val safeMap = value.entries
                        .filter { it.key is String? }
                        .associate { it.key as String? to it.value }
                        .let { LinkedHashMap(it) }
                    super.put(key, ConfigSection(safeMap))
                }

                is Map<*, *> -> {
                    val safeMap = value.entries
                        .filter { it.key is String? }
                        .associate { it.key as String? to it.value }
                    super.put(key, ConfigSection(safeMap))
                }

                is List<*> -> super.put(key, parseList(value))
                else -> super.put(key, value)
            }
        }
    }

    private fun parseList(list: List<*>): List<*> {
        val newList: MutableList<Any> = ArrayList()

        for (o in list) {
            if (o == null) continue
            if (o is LinkedHashMap<*, *>) {
                @Suppress("UNCHECKED_CAST")
                newList.add(ConfigSection(o as LinkedHashMap<String?, Any?>))
            } else {
                newList.add(o)
            }
        }

        return newList
    }

    val allMap: Map<String?, Any?>
        /**
         * Get root section as LinkedHashMap
         *
         * @return
         */
        get() = LinkedHashMap(this)

    val all: ConfigSection
        /**
         * Get new instance of config section
         *
         * @return
         */
        get() = ConfigSection(this)

    /**
     * Get object by key. If section does not contain value, return null
     */
    override fun get(key: String?): Any? {
        return this.get<Any?>(key, null)
    }

    /**
     * Get object by key. If section does not contain value, return default value
     *
     * @param key
     * @param defaultValue
     * @return
     */
    fun <T> get(key: String?, defaultValue: T?): T? {
        if (key.isNullOrEmpty()) return defaultValue
        if (super.containsKey(key)) {
            val value = super.get(key)
            if (defaultValue != null && !defaultValue.javaClass.isInstance(value)) {
                if (value is Map<*, *> && defaultValue is ConfigSection) {
                    val safeMap = value.entries
                        .filter { it.key is String? }
                        .associate { it.key as String? to it.value }
                    @Suppress("UNCHECKED_CAST")
                    return ConfigSection(safeMap) as T
                }
            }
            @Suppress("UNCHECKED_CAST")
            return value as T?
        }
        val keys = key.split("\\.".toRegex(), limit = 2).toTypedArray()
        if (!super.containsKey(keys[0])) return defaultValue
        val value = super.get(keys[0])
        if (value is ConfigSection) {
            return value.get<T?>(keys[1], defaultValue)
        } else if (value is Map<*, *>) {
            val safeMap = value.entries
                .filter { it.key is String? }
                .associate { it.key as String? to it.value }
            val section = ConfigSection(safeMap)
            return section.get<T?>(keys[1], defaultValue)
        }
        return defaultValue
    }

    /**
     * Store value into config section
     *
     * @param key
     * @param value
     */
    fun set(key: String, value: Any?) {
        val subKeys = key.split("\\.".toRegex(), limit = 2).toTypedArray()
        if (subKeys.size > 1) {
            var childSection: ConfigSection? = ConfigSection()
            if (this.containsKey(subKeys[0]) && super.get(subKeys[0]) is ConfigSection) childSection = super.get(
                subKeys[0]
            ) as ConfigSection?
            childSection!!.set(subKeys[1], value)
            super.put(subKeys[0], childSection)
        } else super.put(subKeys[0], value)
    }

    /**
     * Check type of section element defined by key. Return true this element is ConfigSection
     *
     * @param key
     * @return
     */
    fun isSection(key: String?): Boolean {
        val value = this[key]
        return value is ConfigSection
    }

    /**
     * Get config section element defined by key
     *
     * @param key
     * @return
     */
    fun getSection(key: String?): ConfigSection {
        return get(key, ConfigSection())!!
    }

    val sections: ConfigSection
        /**
         * Get all ConfigSections in root path.
         * Example config:
         * a1:
         * b1:
         * c1:
         * c2:
         * a2:
         * b2:
         * c3:
         * c4:
         * a3: true
         * a4: "hello"
         * a5: 100
         *
         *
         * getSections() will return new ConfigSection, that contains sections a1 and a2 only.
         *
         * @return
         */
        get() = getSections(null)

    /**
     * Get sections (and only sections) from provided path
     *
     * @param key - config section path, if null or empty root path will be used.
     * @return
     */
    fun getSections(key: String?): ConfigSection {
        val sections = ConfigSection()
        val parent = if (key.isNullOrEmpty()) this.all else getSection(key)
        parent.forEach { (key1, value) ->
            if (value is ConfigSection) sections[key1] = value
        }
        return sections
    }

    /**
     * Get int value of config section element
     *
     * @param key - key (inside) current section (default value equals to 0)
     * @return
     */
    fun getInt(key: String?): Int {
        return this.getInt(key, 0)
    }

    /**
     * Get int value of config section element
     *
     * @param key          - key (inside) current section
     * @param defaultValue - default value that will return if section element is not exists
     * @return
     */
    fun getInt(key: String?, defaultValue: Int): Int {
        return get(key, (defaultValue as Number))!!.toInt()
    }

    /**
     * Check type of section element defined by key. Return true this element is Integer
     *
     * @param key
     * @return
     */
    fun isInt(key: String?): Boolean {
        val `val` = get(key)
        return `val` is Int
    }

    /**
     * Get long value of config section element
     *
     * @param key - key (inside) current section
     * @return
     */
    fun getLong(key: String?): Long {
        return this.getLong(key, 0)
    }

    /**
     * Get long value of config section element
     *
     * @param key          - key (inside) current section
     * @param defaultValue - default value that will return if section element is not exists
     * @return
     */
    fun getLong(key: String?, defaultValue: Long): Long {
        return get(key, (defaultValue as Number))!!.toLong()
    }

    /**
     * Check type of section element defined by key. Return true this element is Long
     *
     * @param key
     * @return
     */
    fun isLong(key: String?): Boolean {
        val `val` = get(key)
        return `val` is Long
    }

    /**
     * Get double value of config section element
     *
     * @param key - key (inside) current section
     * @return
     */
    fun getDouble(key: String?): Double {
        return this.getDouble(key, 0.0)
    }

    /**
     * Get double value of config section element
     *
     * @param key          - key (inside) current section
     * @param defaultValue - default value that will return if section element is not exists
     * @return
     */
    fun getDouble(key: String?, defaultValue: Double): Double {
        return get(key, (defaultValue as Number))!!.toDouble()
    }

    /**
     * Check type of section element defined by key. Return true this element is Double
     *
     * @param key
     * @return
     */
    fun isDouble(key: String?): Boolean {
        val `val` = get(key)
        return `val` is Double
    }

    /**
     * Get String value of config section element
     *
     * @param key - key (inside) current section
     * @return
     */
    fun getString(key: String?): String {
        return this.getString(key, "")
    }

    /**
     * Get String value of config section element
     *
     * @param key          - key (inside) current section
     * @param defaultValue - default value that will return if section element is not exists
     * @return
     */
    fun getString(key: String?, defaultValue: String): String {
        val result: Any = get(key, defaultValue)!!
        return result.toString()
    }

    /**
     * Check type of section element defined by key. Return true this element is String
     *
     * @param key
     * @return
     */
    fun isString(key: String?): Boolean {
        val `val` = get(key)
        return `val` is String
    }

    /**
     * Get boolean value of config section element
     *
     * @param key - key (inside) current section
     * @return
     */
    fun getBoolean(key: String?): Boolean {
        return this.getBoolean(key, false)
    }

    /**
     * Get boolean value of config section element
     *
     * @param key          - key (inside) current section
     * @param defaultValue - default value that will return if section element is not exists
     * @return
     */
    fun getBoolean(key: String?, defaultValue: Boolean): Boolean {
        return get(key, defaultValue)!!
    }

    /**
     * Check type of section element defined by key. Return true this element is Integer
     *
     * @param key
     * @return
     */
    fun isBoolean(key: String?): Boolean {
        val `val` = get(key)
        return `val` is Boolean
    }

    /**
     * Get List value of config section element
     *
     * @param key - key (inside) current section
     * @return
     */
    fun getList(key: String?): List<*> {
        return this.getList(key, null)
    }

    /**
     * Get List value of config section element
     *
     * @param key         - key (inside) current section
     * @param defaultList - default value that will return if section element is not exists
     * @return
     */
    fun getList(key: String?, defaultList: List<*>?): List<*> {
        return get(key, defaultList)!!
    }

    /**
     * Check type of section element defined by key. Return true this element is List
     *
     * @param key
     * @return
     */
    fun isList(key: String?): Boolean {
        val value = get(key)
        return value is List<*>
    }

    /**
     * Get String List value of config section element
     *
     * @param key - key (inside) current section
     * @return
     */
    fun getStringList(key: String?): List<String> {
        val value = this.getList(key)
        val result: MutableList<String> = ArrayList()
        for (o in value) {
            if (o is String || o is Number || o is Boolean || o is Char) {
                result.add(o.toString())
            }
        }
        return result
    }

    /**
     * Get Integer List value of config section element
     *
     * @param key - key (inside) current section
     * @return
     */
    fun getIntegerList(key: String?): List<Int> {
        val list = getList(key)
        val result: MutableList<Int> = ArrayList()

        for (obj in list) {
            when (obj) {
                is Int -> result.add(obj)
                is Number -> result.add(obj.toInt())
                is Char -> result.add(obj.code)
                is String -> {
                    try {
                        result.add(obj.toInt())
                    } catch (_: Exception) {
                    }
                }
            }
        }
        return result
    }

    /**
     * Get Boolean List value of config section element
     *
     * @param key - key (inside) current section
     * @return
     */
    fun getBooleanList(key: String?): List<Boolean> {
        val list = getList(key)
        val result: MutableList<Boolean> = ArrayList()
        for (obj in list) {
            if (obj is Boolean) {
                result.add(obj)
            } else if (obj is String) {
                if (java.lang.Boolean.TRUE.toString() == obj) {
                    result.add(true)
                } else if (java.lang.Boolean.FALSE.toString() == obj) {
                    result.add(false)
                }
            }
        }
        return result
    }

    /**
     * Get Double List value of config section element
     *
     * @param key - key (inside) current section
     * @return
     */
    fun getDoubleList(key: String?): List<Double> {
        val list = getList(key)
        val result: MutableList<Double> = ArrayList()
        for (obj in list) {
            when (obj) {
                is Double -> result.add(obj)
                is Number -> result.add(obj.toDouble())
                is Char -> result.add(obj.code.toDouble())
                is String -> {
                    try {
                        result.add(obj.toDouble())
                    } catch (_: Exception) {
                    }
                }
            }
        }
        return result
    }

    /**
     * Get Float List value of config section element
     *
     * @param key - key (inside) current section
     * @return
     */
    fun getFloatList(key: String?): List<Float> {
        val list = getList(key)
        val result: MutableList<Float> = ArrayList()
        for (obj in list) {
            when (obj) {
                is Float -> result.add(obj)
                is Number -> result.add(obj.toFloat())
                is Char -> result.add(obj.code.toFloat())
                is String -> {
                    try {
                        result.add(obj.toFloat())
                    } catch (_: Exception) {
                    }
                }
            }
        }
        return result
    }

    /**
     * Get Long List value of config section element
     *
     * @param key - key (inside) current section
     * @return
     */
    fun getLongList(key: String?): List<Long> {
        val list = getList(key)
        val result: MutableList<Long> = ArrayList()
        for (obj in list) {
            when (obj) {
                is Long -> result.add(obj)
                is Number -> result.add(obj.toLong())
                is Char -> result.add(obj.code.toLong())
                is String -> {
                    try {
                        result.add(obj.toLong())
                    } catch (_: Exception) {
                    }
                }
            }
        }
        return result
    }

    /**
     * Get Byte List value of config section element
     *
     * @param key - key (inside) current section
     * @return
     */
    fun getByteList(key: String?): List<Byte> {
        val list = getList(key)

        val result: MutableList<Byte> = ArrayList()

        for (obj in list) {
            when (obj) {
                is Byte -> result.add(obj)
                is Number -> result.add(obj.toByte())
                is Char -> result.add(obj.code.toByte())
                is String -> {
                    try {
                        result.add(obj.toByte())
                    } catch (_: Exception) {
                    }
                }
            }
        }

        return result
    }

    /**
     * Get Character List value of config section element
     *
     * @param key - key (inside) current section
     * @return
     */
    fun getCharacterList(key: String?): List<Char> {
        val list = getList(key)

        val result: MutableList<Char> = ArrayList()

        for (obj in list) {
            if (obj is Char) {
                result.add(obj)
            } else if (obj is String) {
                if (obj.length == 1) {
                    result.add(obj[0])
                }
            } else if (obj is Number) {
                result.add(obj.toInt().toChar())
            }
        }

        return result
    }

    /**
     * Get Short List value of config section element
     *
     * @param key - key (inside) current section
     * @return
     */
    fun getShortList(key: String?): List<Short> {
        val list = getList(key)

        val result: MutableList<Short> = ArrayList()

        for (obj in list) {
            when (obj) {
                is Short -> result.add(obj)
                is Number -> result.add(obj.toShort())
                is Char -> result.add(obj.code.toShort())
                is String -> {
                    try {
                        result.add(obj.toShort())
                    } catch (_: Exception) {
                    }
                }
            }
        }

        return result
    }

    /**
     * Get Map List value of config section element
     *
     * @param key - key (inside) current section
     * @return
     */
    fun getMapList(key: String?): List<Map<*, *>> {
        @Suppress("UNCHECKED_CAST")
        val list = getList(key) as List<Map<*, *>>
        val result: MutableList<Map<*, *>> = ArrayList()

        for (obj in list) {
            result.add(obj)
        }

        return result
    }

    /**
     * Check existence of config section element
     *
     * @param key
     * @param ignoreCase
     * @return
     */
    @JvmOverloads
    fun exists(key: String, ignoreCase: Boolean = false): Boolean {
        var key1 = key
        if (ignoreCase) key1 = key1.lowercase()
        for (existKey in this.getKeys(true)) {
            var existKey1 = existKey
            if (ignoreCase) existKey1 = existKey1.lowercase()
            if (existKey1 == key1) return true
        }
        return false
    }

    /**
     * Remove config section element
     *
     * @param key
     */
    override fun remove(key: String?) {
        if (key.isNullOrEmpty()) return
        if (super.containsKey(key)) super.remove(key)
        else if (this.containsKey(".")) {
            val keys = key.split("\\.".toRegex(), limit = 2).toTypedArray()
            if (super.get(keys[0]) is ConfigSection) {
                val section = super.get(keys[0]) as ConfigSection?
                section!!.remove(keys[1])
            }
        }
    }

    /**
     * Get all keys
     *
     * @param child - true = include child keys
     * @return
     */
    fun getKeys(child: Boolean): Set<String> {
        val keys: MutableSet<String> = LinkedHashSet()
        this.forEach { (key: String?, value: Any?) ->
            keys.add(key!!)
            if (value is ConfigSection) {
                if (child) value.getKeys(true).forEach(Consumer { childKey: String -> keys.add("$key.$childKey") })
            }
        }
        return keys
    }
}
