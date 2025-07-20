package org.chorus_oss.chorus.utils

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import org.chorus_oss.chorus.utils.exception.FormativeRuntimeException
import java.io.*
import java.lang.reflect.Type
import java.math.BigDecimal

/**
 * Gson Tool Class
 *
 *
 * Advantages:
 * <br></br>
 * When the data volume is less than 10000, there is an absolute advantage in speed
 * <br></br>
 * The API and annotation support are relatively comprehensive, supporting loose parsing
 * <br></br>
 * Supports a wide range of data sources (strings, objects, files, streams, readers)
 */
object JSONUtils {
    private val GSON: Gson
    private val PRETTY_GSON: Gson

    init {
        val gsonBuilder = GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss")
        gsonBuilder.disableHtmlEscaping() // 禁止将部分特殊字符转义为unicode编码
        registerTypeAdapter(gsonBuilder)
        GSON = gsonBuilder.create()

        gsonBuilder.setPrettyPrinting()
        PRETTY_GSON = gsonBuilder.create()
    }

    private fun registerTypeAdapter(gsonBuilder: GsonBuilder) {
        gsonBuilder.registerTypeAdapter(
            Short::class.javaPrimitiveType, NumberTypeAdapter(
                Short::class.javaPrimitiveType
            )
        )
        gsonBuilder.registerTypeAdapter(Short::class.java, NumberTypeAdapter(Short::class.java))
        gsonBuilder.registerTypeAdapter(
            Int::class.javaPrimitiveType, NumberTypeAdapter(
                Int::class.javaPrimitiveType
            )
        )
        gsonBuilder.registerTypeAdapter(Int::class.java, NumberTypeAdapter(Int::class.java))
        gsonBuilder.registerTypeAdapter(
            Long::class.javaPrimitiveType, NumberTypeAdapter(
                Long::class.javaPrimitiveType
            )
        )
        gsonBuilder.registerTypeAdapter(Long::class.java, NumberTypeAdapter(Long::class.java))
        gsonBuilder.registerTypeAdapter(
            Float::class.javaPrimitiveType, NumberTypeAdapter(
                Float::class.javaPrimitiveType
            )
        )
        gsonBuilder.registerTypeAdapter(Float::class.java, NumberTypeAdapter(Float::class.java))
        gsonBuilder.registerTypeAdapter(
            Double::class.javaPrimitiveType, NumberTypeAdapter(
                Double::class.javaPrimitiveType
            )
        )
        gsonBuilder.registerTypeAdapter(Double::class.java, NumberTypeAdapter(Double::class.java))
        gsonBuilder.registerTypeAdapter(
            BigDecimal::class.java, NumberTypeAdapter(
                BigDecimal::class.java
            )
        )
    }

    /**
     * JSON deserialization
     */
    fun <V> from(reader: Reader, type: Class<V>?): V {
        val jsonReader = JsonReader(reader)
        return GSON.fromJson(jsonReader, type)
    }

    /**
     * JSON deserialization
     */
    fun <V> from(reader: Reader, typeToken: TypeToken<V>?): V {
        val jsonReader = JsonReader(reader)
        return GSON.fromJson(jsonReader, typeToken)
    }

    /**
     * JSON deserialization
     */
    fun <V> from(inputStream: InputStream, type: Class<V>?): V {
        val reader = JsonReader(InputStreamReader(inputStream))
        return GSON.fromJson(reader, type)
    }

    /**
     * JSON deserialization
     */
    fun <V> from(inputStream: InputStream, typeToken: TypeToken<V>): V {
        val reader = JsonReader(InputStreamReader(inputStream))
        return GSON.fromJson(reader, typeToken.type)
    }

    /**
     * JSON deserialization
     */
    fun <V> from(file: File, type: Class<V>): V {
        try {
            val reader = JsonReader(FileReader(file))
            return GSON.fromJson(reader, type)
        } catch (e: FileNotFoundException) {
            throw GsonException("gson from error, file path: {}, type: {}", file.path, type, e)
        }
    }

    /**
     * JSON deserialization
     */
    fun <V> from(file: File, typeToken: TypeToken<V>): V {
        try {
            val reader = JsonReader(FileReader(file))
            return GSON.fromJson(reader, typeToken.type)
        } catch (e: FileNotFoundException) {
            throw GsonException("gson from error, file path: {}, type: {}", file.path, typeToken.type, e)
        }
    }

    /**
     * JSON deserialization
     */
    fun <V> from(json: String?, type: Class<V>?): V {
        return GSON.fromJson(json, type)
    }

    /**
     * JSON deserialization
     */
    fun <V> from(json: String?, type: Type?): V {
        return GSON.fromJson(json, type)
    }

    /**
     * JSON deserialization
     */
    fun <V> from(json: String?, typeToken: TypeToken<V>): V {
        return GSON.fromJson(json, typeToken.type)
    }

    /**
     * Serialized to JSON
     */
    fun <V> to(list: List<V>?): String {
        return GSON.toJson(list)
    }

    /**
     * Serialized to JSON
     */
    fun <V> to(v: V): String {
        return GSON.toJson(v)
    }

    /**
     * Serialized to JSON of Pretty format
     */
    fun <V> toPretty(v: V): String {
        return PRETTY_GSON.toJson(v)
    }

    /**
     * Add element to the json
     */
    fun <V> add(json: String, key: String, value: V): String {
        val element = JsonParser.parseString(json)
        val jsonObject = element.asJsonObject
        add(jsonObject, key, value)
        return jsonObject.toString()
    }

    /**
     * Add element to the json
     */
    private fun <V> add(jsonObject: JsonObject, key: String, value: V) {
        when (value) {
            is String -> jsonObject.addProperty(key, value as String)
            is Number -> jsonObject.addProperty(key, value as Number)
            else -> jsonObject.addProperty(key, to(value))
        }
    }

    /**
     * remove an element from the json string
     *
     * @return json
     */
    fun remove(json: String, key: String): String {
        val element = JsonParser.parseString(json)
        val jsonObj = element.asJsonObject
        jsonObj.remove(key)
        return jsonObj.toString()
    }

    /**
     * update an element from the json string
     */
    fun <V> update(json: String, key: String, value: V): String {
        val element = JsonParser.parseString(json)
        val jsonObject = element.asJsonObject
        jsonObject.remove(key)
        add(jsonObject, key, value)
        return jsonObject.toString()
    }

    /**
     * Formatting Json (Beautifying)
     *
     * @return json
     */
    fun format(json: String): String {
        val jsonElement = JsonParser.parseString(json)
        return PRETTY_GSON.toJson(jsonElement)
    }

    private class NumberTypeAdapter<T>(private val c: Class<T>?) : TypeAdapter<Number?>() {
        @Throws(IOException::class)
        override fun write(jsonWriter: JsonWriter, number: Number?) {
            if (number != null) {
                jsonWriter.value(number)
            } else {
                jsonWriter.nullValue()
            }
        }

        override fun read(jsonReader: JsonReader): Number? {
            try {
                if (jsonReader.peek() == null) {
                    return null
                }
                val json = jsonReader.nextString()
                when (c) {
                    Short::class.javaPrimitiveType -> {
                        return json.toShort()
                    }

                    Short::class.java -> {
                        if (json.isEmpty()) {
                            return null
                        }
                        return json.toShort()
                    }

                    Int::class.javaPrimitiveType -> {
                        return json.toInt()
                    }

                    Int::class.java -> {
                        if (json.isEmpty()) {
                            return null
                        }
                        return json.toInt()
                    }

                    Long::class.javaPrimitiveType -> {
                        return json.toLong()
                    }

                    Long::class.java -> {
                        if (json.isEmpty()) {
                            return null
                        }
                        return json.toLong()
                    }

                    Float::class.javaPrimitiveType -> {
                        return json.toFloat()
                    }

                    Float::class.java -> {
                        if (json.isEmpty()) {
                            return null
                        }
                        return json.toFloat()
                    }

                    Double::class.javaPrimitiveType -> {
                        return json.toDouble()
                    }

                    Double::class.java -> {
                        if (json.isEmpty()) {
                            return null
                        }
                        return json.toDouble()
                    }

                    BigDecimal::class.java -> {
                        if (json.isEmpty()) {
                            return null
                        }
                        return BigDecimal(json)
                    }

                    else -> {
                        return json.toInt()
                    }
                }
            } catch (_: Exception) {
                return null
            }
        }
    }


    class GsonException : FormativeRuntimeException {
        constructor(format: String, vararg arguments: Any) : super(format, *arguments)
    }
}