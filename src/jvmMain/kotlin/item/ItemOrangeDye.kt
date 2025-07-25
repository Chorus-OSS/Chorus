package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.utils.DyeColor

class ItemOrangeDye : ItemDye(ItemID.Companion.ORANGE_DYE) {
    override val dyeColor: DyeColor
        get() = DyeColor.ORANGE

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}