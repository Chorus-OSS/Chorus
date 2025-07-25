package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.item.ItemTool

class BlockShroomlight(blockState: BlockState = properties.defaultState) : BlockTransparent(blockState) {
    override val name: String
        get() = "Shroomlight"

    override val toolType: Int
        get() = ItemTool.TYPE_HOE

    override val resistance: Double
        get() = 1.0

    override val hardness: Double
        get() = 1.0

    override val lightLevel: Int
        get() = 15

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.SHROOMLIGHT)
    }
}
