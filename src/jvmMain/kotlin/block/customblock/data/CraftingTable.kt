package org.chorus_oss.chorus.block.customblock.data

import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.ListTag
import org.chorus_oss.chorus.nbt.tag.StringTag
import java.util.function.Consumer

@JvmRecord
data class CraftingTable(val tableName: String, val craftingTags: List<String>?) : NBTData {
    override fun toCompoundTag(): CompoundTag {
        val listTag = ListTag<StringTag>()
        craftingTags?.forEach(Consumer { listTag.add(StringTag(it)) })
        return CompoundTag()
            .putList("crafting_tags", listTag)
            .putString("table_name", tableName)
    }
}
