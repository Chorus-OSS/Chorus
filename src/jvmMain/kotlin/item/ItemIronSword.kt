package org.chorus_oss.chorus.item

class ItemIronSword @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    ItemTool(ItemID.Companion.IRON_SWORD, meta, count, "Iron Sword") {
    override val maxDurability: Int
        get() = DURABILITY_IRON

    override val isSword: Boolean
        get() = true

    override val tier: Int
        get() = TIER_IRON

    override val attackDamage: Int
        get() = 6
}