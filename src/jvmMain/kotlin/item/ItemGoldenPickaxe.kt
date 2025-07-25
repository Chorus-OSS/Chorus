package org.chorus_oss.chorus.item

class ItemGoldenPickaxe @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    ItemTool(ItemID.Companion.GOLDEN_PICKAXE, meta, count, "Golden Pickaxe") {
    override val maxDurability: Int
        get() = DURABILITY_GOLD

    override val isPickaxe: Boolean
        get() = true

    override val tier: Int
        get() = TIER_GOLD

    override val attackDamage: Int
        get() = 2
}