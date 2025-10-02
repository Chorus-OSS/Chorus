package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.utils.DyeColor

class ItemRedDye : ItemDye(ItemID.RED_DYE) {
    override val dyeColor: DyeColor
        get() = DyeColor.RED

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}