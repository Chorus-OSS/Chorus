package org.chorus_oss.chorus.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.IOException
import java.io.Reader
import java.lang.reflect.Type
import java.math.BigDecimal

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

    fun <V> from(reader: Reader, typeToken: TypeToken<V>?): V {
        val jsonReader = JsonReader(reader)
        return GSON.fromJson(jsonReader, typeToken)
    }

    fun <V> from(reader: Reader, type: Class<V>?): V {
        val jsonReader = JsonReader(reader)
        return GSON.fromJson(jsonReader, type)
    }

    fun <V> from(json: String?, type: Type?): V {
        return GSON.fromJson(json, type)
    }

    fun <V> from(file: File, type: Class<V>): V {
        try {
            val reader = JsonReader(FileReader(file))
            return GSON.fromJson(reader, type)
        } catch (e: FileNotFoundException) {
            throw RuntimeException("gson from error, file path: ${file.path}, type: $type", e)
        }
    }


    fun <V> to(list: List<V>?): String {
        return GSON.toJson(list)
    }

    fun <V> toPretty(v: V): String {
        return PRETTY_GSON.toJson(v)
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
}