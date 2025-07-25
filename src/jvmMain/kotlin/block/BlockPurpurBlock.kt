package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.item.ItemTool

class BlockPurpurBlock @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockSolid(blockstate) {
    override val name: String
        get() = "Purpur"

    override val hardness: Double
        get() = 1.5

    override val resistance: Double
        get() = 30.0

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override val toolTier: Int
        get() = ItemTool.TIER_WOODEN


    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.PURPUR_BLOCK, CommonBlockProperties.PILLAR_AXIS)
    }
}