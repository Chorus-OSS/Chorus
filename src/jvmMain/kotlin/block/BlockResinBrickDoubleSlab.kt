package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockResinBrickDoubleSlab(blockstate: BlockState) : BlockDoubleWoodenSlab(blockstate) {
    override fun getSlabName() = "Resin Brick"

    override fun getSingleSlab() = BlockResinBrickSlab.properties.defaultState

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.RESIN_BRICK_DOUBLE_SLAB, CommonBlockProperties.MINECRAFT_VERTICAL_HALF)
    }
}