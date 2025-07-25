package org.chorus_oss.chorus.item

class ItemDiamondPickaxe : ItemTool(ItemID.Companion.DIAMOND_PICKAXE) {
    override val maxDurability: Int
        get() = DURABILITY_DIAMOND

    override val isPickaxe: Boolean
        get() = true

    override val tier: Int
        get() = TIER_DIAMOND

    override val attackDamage: Int
        get() = 5
}