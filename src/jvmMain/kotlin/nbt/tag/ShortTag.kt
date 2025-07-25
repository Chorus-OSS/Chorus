package org.chorus_oss.chorus.nbt.tag

import java.util.*

class ShortTag : NumberTag<Short> {
    override var data: Short = 0

    constructor()

    constructor(data: Int) {
        this.data = data.toShort()
    }

    override fun parseValue(): Short {
        return data
    }

    override val id: Byte
        get() = TAG_SHORT

    override fun toString(): String {
        return "ShortTag (data: $data)"
    }

    override fun toSNBT(): String {
        return data.toString() + "s"
    }

    override fun toSNBT(space: Int): String {
        return data.toString() + "s"
    }

    override fun copy(): ShortTag {
        return ShortTag(data.toInt())
    }

    override fun equals(other: Any?): Boolean {
        if (super.equals(other)) {
            val o = other as ShortTag
            return data == o.data
        }
        return false
    }

    override fun hashCode(): Int {
        return Objects.hash(super.hashCode(), data, id)
    }
}
