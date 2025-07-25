package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.item.ItemTool

class BlockFletchingTable @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockSolid(blockstate) {
    override val name: String
        get() = "Fletching Table"

    override val toolType: Int
        get() = ItemTool.TYPE_AXE

    override val resistance: Double
        get() = 12.5

    override val hardness: Double
        get() = 2.5

    override val burnChance: Int
        get() = 5

    override fun canHarvestWithHand(): Boolean {
        return true
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.FLETCHING_TABLE)
    }
}
