package org.chorus_oss.chorus.item

class ItemDiamondAxe : ItemTool(ItemID.Companion.DIAMOND_AXE) {
    override val maxDurability: Int
        get() = DURABILITY_DIAMOND

    override val isAxe: Boolean
        get() = true

    override val tier: Int
        get() = TIER_DIAMOND

    override val attackDamage: Int
        get() = 6

    override fun canBreakShield(): Boolean {
        return true
    }
}