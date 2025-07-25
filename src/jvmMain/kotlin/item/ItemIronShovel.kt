package org.chorus_oss.chorus.item

class ItemIronShovel @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    ItemTool(ItemID.Companion.IRON_SHOVEL, meta, count, "Iron Shovel") {
    override val maxDurability: Int
        get() = DURABILITY_IRON

    override val isShovel: Boolean
        get() = true

    override val tier: Int
        get() = TIER_IRON

    override val attackDamage: Int
        get() = 3
}