package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.utils.DyeColor

class ItemLightBlueDye : ItemDye(ItemID.Companion.LIGHT_BLUE_DYE) {
    override val dyeColor: DyeColor
        get() = DyeColor.LIGHT_BLUE

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}