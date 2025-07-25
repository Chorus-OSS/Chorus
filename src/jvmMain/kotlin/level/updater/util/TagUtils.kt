package org.chorus_oss.chorus.level.updater.util

import org.chorus_oss.chorus.nbt.tag.*

object TagUtils {
    fun toMutable(immutable: Tag<*>): Any {
        return when (immutable) {
            is CompoundTag -> {
                val mutable: MutableMap<String, Any> = LinkedHashMap()
                immutable.tags.forEach { (k, v) -> mutable[k] = toMutable(v) }
                mutable
            }

            is ListTag<*> -> {
                val list: MutableList<Any> = ArrayList()
                immutable.all.forEach(({ v -> list.add(toMutable(v)) }))
                list
            }

            is ByteTag -> (immutable.parseValue() and 0xff).toByte()
            is ShortTag -> (immutable.parseValue().toInt() and 0xffff).toShort()
            else -> immutable.parseValue()
        }
    }

    fun toImmutable(mutable: Any?): Tag<*> {
        return when (mutable) {
            is Map<*, *> -> {
                val compoundTag = CompoundTag()
                mutable.forEach { k, v ->
                    if (k is String) {
                        compoundTag.put(k, toImmutable(v))
                    }
                }
                compoundTag
            }

            is List<*> -> {
                val listTag = ListTag<Tag<*>>()
                mutable.forEach { listTag.add(toImmutable(it)) }
                listTag
            }

            else -> byClass(mutable)
        }
    }

    private fun byClass(mutable: Any?): Tag<*> {
        return when (mutable) {
            is Int -> IntTag(mutable)
            is Byte -> ByteTag(mutable.toInt())
            is Short -> ShortTag(mutable.toInt())
            is Long -> LongTag(mutable)
            is Float -> FloatTag(mutable)
            is Double -> DoubleTag(mutable)
            is String -> StringTag(mutable)
            is ByteArray -> ByteArrayTag(mutable)
            is IntArray -> IntArrayTag(mutable)
            null -> EndTag()
            else -> throw IllegalArgumentException("unhandled error in TagUtils")
        }
    }
}
