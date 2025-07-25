package org.chorus_oss.chorus.nbt.tag

import java.util.*

class ByteTag : NumberTag<Int> {
    override var data: Int = 0

    constructor()

    constructor(data: Int) {
        this.data = data.toByte().toInt()
    }

    fun getData(): Int {
        return data
    }

    fun setData(data: Int?) {
        this.data = (data ?: 0).toByte().toInt()
    }

    override val id: Byte
        get() = TAG_BYTE

    override fun parseValue(): Int {
        return data
    }

    override fun toString(): String {
        var hex = Integer.toHexString(this.data)
        if (hex.length < 2) {
            hex = "0$hex"
        }
        return "ByteTag  (data: 0x$hex)"
    }

    override fun toSNBT(): String {
        return data.toString() + "b"
    }

    override fun toSNBT(space: Int): String {
        return data.toString() + "b"
    }

    override fun equals(other: Any?): Boolean {
        if (super.equals(other)) {
            val byteTag = other as ByteTag
            return data == byteTag.data
        }
        return false
    }

    override fun hashCode(): Int {
        return Objects.hash(super.hashCode(), data, id)
    }

    override fun copy(): Tag<Int> {
        return ByteTag(data)
    }

}
