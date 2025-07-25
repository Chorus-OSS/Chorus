package org.chorus_oss.chorus.item

class ItemWarpedFungusOnAStick @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    ItemTool(ItemID.Companion.WARPED_FUNGUS_ON_A_STICK, meta, count, "Warped Fungus on a Stick") {
    override val maxStackSize: Int
        get() = 1

    override val maxDurability: Int
        get() = DURABILITY_WARPED_FUNGUS_ON_A_STICK

    override fun noDamageOnBreak(): Boolean {
        return true
    }
}
