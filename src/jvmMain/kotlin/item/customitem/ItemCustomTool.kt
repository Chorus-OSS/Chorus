package org.chorus_oss.chorus.item.customitem

import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.registry.Registries

abstract class ItemCustomTool(id: String) : ItemTool(id), CustomItem {
    override val maxDurability: Int
        get() = DURABILITY_WOODEN

    val speed: Int?
        get() {
            val nbt = Registries.ITEM.customItemDefinition[this.id]!!.nbt
            if (!nbt.getCompound("components").contains("minecraft:digger")) return null
            return nbt.getCompound("components")
                .getCompound("minecraft:digger")
                .getList("destroy_speeds", CompoundTag::class.java)[0].getInt("speed")
        }
}
