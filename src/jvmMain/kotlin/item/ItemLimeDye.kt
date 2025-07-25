package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.utils.DyeColor

class ItemLimeDye : ItemDye(ItemID.Companion.LIME_DYE) {
    override val dyeColor: DyeColor
        get() = DyeColor.LIME

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}