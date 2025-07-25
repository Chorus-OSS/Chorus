package org.chorus_oss.chorus.item

class ItemBrush : ItemTool(ItemID.Companion.BRUSH) {
    override fun canBeActivated(): Boolean {
        return true
    }

    override val maxDurability: Int
        get() = 65
}