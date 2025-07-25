package org.chorus_oss.chorus.nbt.tag

import java.util.*

class LongTag : NumberTag<Long> {
    override var data: Long = 0

    constructor()

    constructor(data: Long) {
        this.data = data
    }

    override fun parseValue(): Long {
        return this.data
    }

    override val id: Byte
        get() = TAG_LONG

    override fun toString(): String {
        return "LongTag  (data:$data)"
    }

    override fun toSNBT(): String {
        return data.toString() + "L"
    }

    override fun toSNBT(space: Int): String {
        return data.toString() + "L"
    }

    override fun copy(): LongTag {
        return LongTag(data)
    }

    override fun equals(other: Any?): Boolean {
        if (super.equals(other)) {
            val o = other as LongTag
            return data == o.data
        }
        return false
    }

    override fun hashCode(): Int {
        return Objects.hash(super.hashCode(), data, id)
    }
}
