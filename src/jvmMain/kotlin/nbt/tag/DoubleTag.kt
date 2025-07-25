package org.chorus_oss.chorus.nbt.tag

import java.util.*

class DoubleTag : NumberTag<Double> {
    override var data: Double = 0.0

    constructor()

    constructor(data: Double) {
        this.data = data
    }

    override fun parseValue(): Double {
        return this.data
    }

    override val id: Byte
        get() = TAG_DOUBLE

    override fun toString(): String {
        return "DoubleTag  (data: $data)"
    }

    override fun toSNBT(): String {
        return data.toString() + "d"
    }

    override fun toSNBT(space: Int): String {
        return data.toString() + "d"
    }

    override fun copy(): DoubleTag {
        return DoubleTag(data)
    }

    override fun equals(other: Any?): Boolean {
        if (super.equals(other)) {
            val o = other as DoubleTag
            return data == o.data
        }
        return false
    }

    override fun hashCode(): Int {
        return Objects.hash(super.hashCode(), data, id)
    }
}
