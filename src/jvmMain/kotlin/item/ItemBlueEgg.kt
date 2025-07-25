package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.entity.ClimateVariant
import org.chorus_oss.chorus.nbt.tag.CompoundTag

class ItemBlueEgg(meta: Int = 0, count: Int = 1) : ItemEgg(ItemID.BLUE_EGG, meta, count, "Blue Egg") {
    override fun correctNBT(nbt: CompoundTag) {
        nbt.putString("variant", ClimateVariant.Companion.Variant.Cold.id)
    }
}