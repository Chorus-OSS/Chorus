package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockSpruceDoubleSlab(blockstate: BlockState) : BlockDoubleWoodenSlab(blockstate) {
    override fun getSlabName() = "Spruce"

    override fun getSingleSlab() = BlockSpruceSlab.properties.defaultState

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.SPRUCE_DOUBLE_SLAB, CommonBlockProperties.MINECRAFT_VERTICAL_HALF)
    }
}