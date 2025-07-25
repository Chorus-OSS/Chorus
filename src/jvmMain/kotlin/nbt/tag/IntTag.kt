package org.chorus_oss.chorus.nbt.tag

import java.util.*

class IntTag : NumberTag<Int> {
    override var data: Int = 0

    constructor()

    constructor(data: Int) {
        this.data = data
    }

    override fun parseValue(): Int {
        return this.data
    }

    override val id: Byte
        get() = TAG_INT

    override fun toString(): String {
        return "IntTag (data: $data)"
    }

    override fun toSNBT(): String {
        return data.toString()
    }

    override fun toSNBT(space: Int): String {
        return data.toString()
    }

    override fun copy(): IntTag {
        return IntTag(data)
    }

    override fun equals(other: Any?): Boolean {
        if (super.equals(other)) {
            val o = other as IntTag
            return data == o.data
        }
        return false
    }

    override fun hashCode(): Int {
        return Objects.hash(super.hashCode(), data, id)
    }
}
