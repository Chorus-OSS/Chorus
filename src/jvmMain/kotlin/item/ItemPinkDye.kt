package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.utils.DyeColor

class ItemPinkDye : ItemDye(ItemID.Companion.PINK_DYE) {
    override val dyeColor: DyeColor
        get() = DyeColor.PINK

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}