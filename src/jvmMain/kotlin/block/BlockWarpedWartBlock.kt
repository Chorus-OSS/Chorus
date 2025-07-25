package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.item.ItemTool

class BlockWarpedWartBlock @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockSolid(blockstate) {
    override val name: String
        get() = "Warped Wart Block"

    override val toolType: Int
        get() = ItemTool.TYPE_HOE

    override val resistance: Double
        get() = 1.0

    override val hardness: Double
        get() = 1.0

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.WARPED_WART_BLOCK)
    }
}
