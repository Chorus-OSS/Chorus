package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.utils.DyeColor

class ItemGrayDye : ItemDye(ItemID.Companion.GRAY_DYE) {
    override val dyeColor: DyeColor
        get() = DyeColor.GRAY

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}