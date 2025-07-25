package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.utils.DyeColor

abstract class BlockWool(blockstate: BlockState) : BlockSolid(blockstate) {
    override val name: String
        get() = getDyeColor().colorName + " Wool"

    override val toolType: Int
        get() = ItemTool.TYPE_SHEARS

    override val hardness: Double
        get() = 0.8

    override val resistance: Double
        get() = 0.8

    override val burnChance: Int
        get() = 30

    override val burnAbility: Int
        get() = 60

    open fun getDyeColor(): DyeColor = DyeColor.WHITE
}
