package org.chorus_oss.chorus.item

class ItemIronPickaxe @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    ItemTool(ItemID.Companion.IRON_PICKAXE, meta, count, "Iron Pickaxe") {
    override val maxDurability: Int
        get() = DURABILITY_IRON

    override val isPickaxe: Boolean
        get() = true

    override val tier: Int
        get() = TIER_IRON

    override val attackDamage: Int
        get() = 4
}