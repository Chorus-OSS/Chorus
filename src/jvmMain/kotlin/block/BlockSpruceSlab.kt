package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockSpruceSlab(blockstate: BlockState) : BlockWoodenSlab(blockstate, BlockID.SPRUCE_DOUBLE_SLAB) {
    override fun getSlabName(): String {
        return "Spruce"
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.SPRUCE_SLAB, CommonBlockProperties.MINECRAFT_VERTICAL_HALF)
    }
}