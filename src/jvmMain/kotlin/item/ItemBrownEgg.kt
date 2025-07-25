package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.entity.ClimateVariant
import org.chorus_oss.chorus.nbt.tag.CompoundTag

class ItemBrownEgg(meta: Int = 0, count: Int = 1) : ItemEgg(ItemID.BROWN_EGG, meta, count, "Brown Egg") {
    override fun correctNBT(nbt: CompoundTag) {
        nbt.putString("variant", ClimateVariant.Companion.Variant.Warm.id)
    }
}