package org.chorus_oss.chorus.item

class ItemGoldenSword @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    ItemTool(ItemID.Companion.GOLDEN_SWORD, meta, count, "Golden Sword") {
    override val maxDurability: Int
        get() = DURABILITY_GOLD

    override val isSword: Boolean
        get() = true

    override val tier: Int
        get() = TIER_GOLD

    override val attackDamage: Int
        get() = 4
}