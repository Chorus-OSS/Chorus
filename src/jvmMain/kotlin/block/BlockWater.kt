package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.math.BlockFace

/**
 * Alias still water
 */
class BlockWater @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockFlowingWater(blockstate) {
    override fun place(
        item: Item?,
        block: Block,
        target: Block?,
        face: BlockFace,
        fx: Double,
        fy: Double,
        fz: Double,
        player: Player?
    ): Boolean {
        return level.setBlock(this.position, this, true, false)
    }

    override val name: String
        get() = "Still Water"

    override fun getLiquidWithNewDepth(depth: Int): BlockLiquid {
        return BlockWater(
            blockState.setPropertyValue(
                Companion.properties,
                CommonBlockProperties.LIQUID_DEPTH.createValue(depth)
            )
        )
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.WATER, CommonBlockProperties.LIQUID_DEPTH)

    }
}
