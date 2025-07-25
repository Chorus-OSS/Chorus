package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.item.ItemTool

class BlockNetherWartBlock @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockSolid(blockstate) {
    override val name: String
        get() = "Nether Wart Block"

    override val resistance: Double
        get() = 5.0

    override val hardness: Double
        get() = 1.0

    override val toolType: Int
        get() = ItemTool.TYPE_HOE

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.NETHER_WART_BLOCK)
    }
}
