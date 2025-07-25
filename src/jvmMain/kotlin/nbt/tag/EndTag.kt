package org.chorus_oss.chorus.nbt.tag

class EndTag : Tag<Unit>() {
    override val id: Byte
        get() = TAG_END

    override fun toString(): String {
        return "EndTag"
    }

    override fun toSNBT(): String {
        return ""
    }

    override fun toSNBT(space: Int): String {
        return ""
    }

    override fun copy(): Tag<Unit> {
        return EndTag()
    }

    override fun parseValue() {
        return
    }
}
