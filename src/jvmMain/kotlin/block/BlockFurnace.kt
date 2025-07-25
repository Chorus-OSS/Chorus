package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockFurnace @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockLitFurnace(blockstate) {
    override val name: String
        get() = "Furnace"

    override val lightLevel: Int
        get() = 0

    override fun canHarvestWithHand(): Boolean {
        return false
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.FURNACE, CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION)
    }
}
