package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockBambooMosaicSlab(blockState: BlockState) : BlockWoodenSlab(blockState, BlockID.BAMBOO_MOSAIC_DOUBLE_SLAB) {
    override fun getSlabName(): String {
        return "Bamboo Mosaic"
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.BAMBOO_MOSAIC_SLAB, CommonBlockProperties.MINECRAFT_VERTICAL_HALF)
    }
}