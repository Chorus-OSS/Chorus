package org.chorus_oss.chorus.nbt.tag

import java.util.*

class FloatTag : NumberTag<Float> {
    override var data: Float = 0f

    constructor()

    constructor(data: Double) {
        this.data = data.toFloat()
    }

    constructor(data: Float) {
        this.data = data
    }

    override fun parseValue(): Float {
        return this.data
    }

    override val id: Byte
        get() = TAG_FLOAT

    override fun toString(): String {
        return "FloatTag  (data: $data)"
    }

    override fun toSNBT(): String {
        return data.toString() + "f"
    }

    override fun toSNBT(space: Int): String {
        return data.toString() + "f"
    }

    override fun copy(): FloatTag {
        return FloatTag(data)
    }

    override fun equals(other: Any?): Boolean {
        if (super.equals(other)) {
            val o = other as FloatTag
            return data == o.data
        }
        return false
    }

    override fun hashCode(): Int {
        return Objects.hash(super.hashCode(), data, id)
    }
}
