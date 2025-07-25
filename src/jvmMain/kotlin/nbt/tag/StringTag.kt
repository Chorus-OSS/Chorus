package org.chorus_oss.chorus.nbt.tag

import java.util.*

class StringTag(var data: String) : Tag<String>() {
    constructor() : this("")

    override fun parseValue(): String {
        return this.data
    }

    override val id: Byte
        get() = TAG_STRING

    override fun toString(): String {
        return "StringTag  (data: $data)"
    }

    override fun toSNBT(): String {
        return "\"" + data + "\""
    }

    override fun toSNBT(space: Int): String {
        return "\"" + data + "\""
    }

    override fun copy(): Tag<String> {
        return StringTag(data)
    }

    override fun equals(other: Any?): Boolean {
        if (super.equals(other)) {
            val o = other as StringTag
            return (data == o.data)
        }
        return false
    }

    override fun hashCode(): Int {
        return Objects.hash(super.hashCode(), data)
    }
}
