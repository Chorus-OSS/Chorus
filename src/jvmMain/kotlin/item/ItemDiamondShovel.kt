package org.chorus_oss.chorus.item

class ItemDiamondShovel : ItemTool(ItemID.Companion.DIAMOND_SHOVEL) {
    override val maxDurability: Int
        get() = DURABILITY_DIAMOND

    override val isShovel: Boolean
        get() = true

    override val tier: Int
        get() = TIER_DIAMOND

    override val attackDamage: Int
        get() = 4
}