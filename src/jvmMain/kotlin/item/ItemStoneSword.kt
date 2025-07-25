package org.chorus_oss.chorus.item

class ItemStoneSword @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    ItemTool(ItemID.Companion.STONE_SWORD, meta, count, "Stone Sword") {
    override val maxDurability: Int
        get() = DURABILITY_STONE

    override val isSword: Boolean
        get() = true

    override val tier: Int
        get() = TIER_STONE

    override val attackDamage: Int
        get() = 5
}