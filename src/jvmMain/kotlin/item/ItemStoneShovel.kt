package org.chorus_oss.chorus.item

class ItemStoneShovel @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    ItemTool(ItemID.Companion.STONE_SHOVEL, meta, count, "Stone Shovel") {
    override val maxDurability: Int
        get() = DURABILITY_STONE

    override val isShovel: Boolean
        get() = true

    override val tier: Int
        get() = TIER_STONE

    override val attackDamage: Int
        get() = 2
}