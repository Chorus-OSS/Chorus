package org.chorus_oss.chorus.entity.ai.memory.codec

import org.chorus_oss.chorus.nbt.tag.*
import java.util.function.BiConsumer
import java.util.function.Function

class NumberMemoryCodec<Data : Number?>(key: String) : MemoryCodec<Data>(
    Function { tag ->
        if (tag.contains(key)) (TagReader<Data>(
            tag[key] as NumberTag<*>
        )).data else null
    },
    BiConsumer { data, tag ->
        if (data != null) {
            tag.put(key, newTag(data))
        } else tag.remove(key)
    }
) {
    class TagReader<Data : Number?>(var tag: NumberTag<*>) {
        val data: Data
            get() {
                val data = tag.data

                @Suppress("UNCHECKED_CAST")
                return when (tag) {
                    is ByteTag -> {
                        data.toByte() as Data
                    }

                    is ShortTag -> {
                        data.toShort() as Data
                    }

                    else -> tag.data as Data
                }
            }
    }

    companion object {
        fun newTag(data: Number): NumberTag<*> {
            return when (data) {
                is Byte -> ByteTag(data.toByte().toInt())
                is Short -> ShortTag(data.toShort().toInt())
                is Int -> IntTag(data.toInt())
                is Long -> LongTag(data.toLong())
                is Float -> FloatTag(data.toFloat())
                is Double -> DoubleTag(data.toDouble())
                else -> throw IllegalArgumentException("Unknown number type: " + data.javaClass.name)
            }
        }
    }
}
