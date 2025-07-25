package org.chorus_oss.chorus.item

class ItemDiamondSword : ItemTool(ItemID.Companion.DIAMOND_SWORD) {
    override val maxDurability: Int
        get() = DURABILITY_DIAMOND

    override val isSword: Boolean
        get() = true

    override val tier: Int
        get() = TIER_DIAMOND

    override val attackDamage: Int
        get() = 7
}