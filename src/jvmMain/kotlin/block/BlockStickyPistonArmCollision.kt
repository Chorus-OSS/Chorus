package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockStickyPistonArmCollision @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockPistonArmCollision(blockstate) {
    override val name: String
        get() = "Sticky Piston Head"

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.STICKY_PISTON_ARM_COLLISION, CommonBlockProperties.FACING_DIRECTION)
    }
}