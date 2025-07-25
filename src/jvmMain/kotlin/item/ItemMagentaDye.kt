package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.utils.DyeColor

class ItemMagentaDye : ItemDye(ItemID.Companion.MAGENTA_DYE) {
    override val dyeColor: DyeColor
        get() = DyeColor.MAGENTA

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}