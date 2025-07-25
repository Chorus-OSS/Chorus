package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.item.ItemTool

class BlockSoulSoil @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockSolid(blockstate) {
    override val name: String
        get() = "Soul Soil"

    override val hardness: Double
        get() = 1.0

    override val resistance: Double
        get() = 1.0

    override val toolType: Int
        get() = ItemTool.TYPE_SHOVEL

    override fun canHarvestWithHand(): Boolean {
        return true
    }

    override val isSoulSpeedCompatible: Boolean
        get() = true

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.SOUL_SOIL)
    }
}
