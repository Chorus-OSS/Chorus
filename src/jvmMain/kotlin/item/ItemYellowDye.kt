package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.utils.DyeColor

class ItemYellowDye : ItemDye(ItemID.Companion.YELLOW_DYE) {
    override val dyeColor: DyeColor
        get() = DyeColor.YELLOW

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}