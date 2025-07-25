package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.item.ItemTool

class BlockHoneycombBlock(blockState: BlockState = properties.defaultState) : BlockSolid(blockState) {
    override val hardness: Double
        get() = 0.6

    override val resistance: Double
        get() = 0.6

    override val toolType: Int
        get() = ItemTool.TYPE_HANDS_ONLY

    override fun canHarvestWithHand(): Boolean {
        return true
    }

    override val name: String
        get() = "Honeycomb Block"

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.HONEYCOMB_BLOCK)
    }
}
