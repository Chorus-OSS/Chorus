package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.entity.ClimateVariant
import org.chorus_oss.chorus.nbt.tag.CompoundTag


open class ItemEgg : ProjectileItem {
    constructor(meta: Int = 0, count: Int = 1) : this(ItemID.Companion.EGG, meta, count, "Egg")

    constructor(id: String, meta: Int, count: Int, name: String) : super(id, meta, count, name)

    override val projectileEntityType: String
        get() = ItemID.Companion.EGG

    override val throwForce: Float
        get() = 1.5f

    override val maxStackSize: Int
        get() = 16

    override fun correctNBT(nbt: CompoundTag) {
        nbt.putString("variant", ClimateVariant.Companion.Variant.Temperate.id)
    }
}
