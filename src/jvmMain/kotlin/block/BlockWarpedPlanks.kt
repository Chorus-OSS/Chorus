package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.item.ItemTool

class BlockWarpedPlanks @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockPlanks(blockstate) {
    override val name: String
        get() = "Warped Planks"

    override val hardness: Double
        get() = 2.0

    override val resistance: Double
        get() = 3.0

    override val toolType: Int
        get() = ItemTool.TYPE_AXE

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.WARPED_PLANKS)
    }
}