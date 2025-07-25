package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.item.ItemTool


abstract class BlockPlanks(blockState: BlockState) : BlockSolid(blockState) {
    override val hardness: Double
        get() = 2.0

    override val resistance: Double
        get() = 15.0

    override val burnChance: Int
        get() = 5

    override val burnAbility: Int
        get() = 20

    override val toolType: Int
        get() = ItemTool.TYPE_AXE
}
