package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.type.IntPropertyType
import org.chorus_oss.chorus.math.BlockFace

class BlockPiston @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockPistonBase(blockstate) {
    override val name: String
        get() = "Piston"

    override fun createHead(blockFace: BlockFace): Block {
        return BlockPistonArmCollision().setPropertyValue<Int, IntPropertyType>(
            CommonBlockProperties.FACING_DIRECTION,
            blockFace.index
        )
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.PISTON, CommonBlockProperties.FACING_DIRECTION)
    }
}
