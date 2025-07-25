package org.chorus_oss.chorus.block.customblock.data

import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.ListTag
import org.chorus_oss.chorus.nbt.tag.StringTag

/**
 * The type Permutation builder.
 */
@JvmRecord
data class Permutation(val component: Component, val condition: String?, val blockTags: Array<String>) :
    NBTData {
    constructor(component: Component, condition: String?) : this(component, condition, arrayOf<String>())

    override fun toCompoundTag(): CompoundTag {
        val result = CompoundTag()
            .putCompound("components", component.toCompoundTag())
            .putString("condition", condition!!)
        val stringTagListTag = ListTag<StringTag>()
        for (s in blockTags) {
            stringTagListTag.add(StringTag(s))
        }
        if (stringTagListTag.size() > 0) {
            result.putList("blockTags", stringTagListTag)
        }
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Permutation

        if (component != other.component) return false
        if (condition != other.condition) return false
        if (!blockTags.contentEquals(other.blockTags)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = component.hashCode()
        result = 31 * result + (condition?.hashCode() ?: 0)
        result = 31 * result + blockTags.contentHashCode()
        return result
    }
}
