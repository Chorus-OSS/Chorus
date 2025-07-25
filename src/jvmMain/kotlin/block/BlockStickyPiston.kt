package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.type.IntPropertyType
import org.chorus_oss.chorus.math.BlockFace

class BlockStickyPiston @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockPistonBase(blockstate) {
    init {
        sticky = true
    }

    override fun createHead(blockFace: BlockFace): Block {
        return BlockStickyPistonArmCollision().setPropertyValue<Int, IntPropertyType>(
            CommonBlockProperties.FACING_DIRECTION,
            blockFace.index
        )
    }

    override val name: String
        get() = "Sticky Piston"

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.STICKY_PISTON, CommonBlockProperties.FACING_DIRECTION)
    }
}