package org.chorus_oss.chorus.item

class ItemWoodenShovel @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    ItemTool(ItemID.Companion.WOODEN_SHOVEL, meta, count, "Wooden Shovel") {
    override val maxDurability: Int
        get() = DURABILITY_WOODEN

    override val isShovel: Boolean
        get() = true

    override val tier: Int
        get() = TIER_WOODEN

    override val attackDamage: Int
        get() = 1
}