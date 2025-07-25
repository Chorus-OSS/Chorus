package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.block.BlockCocoa
import org.chorus_oss.chorus.utils.DyeColor

class ItemCocoaBeans : ItemDye(ItemID.COCOA_BEANS) {
    init {
        this.blockState = BlockCocoa.properties.defaultState
    }

    override val dyeColor: DyeColor
        get() = DyeColor.BROWN

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}