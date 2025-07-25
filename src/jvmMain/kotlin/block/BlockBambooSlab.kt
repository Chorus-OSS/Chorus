package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockBambooSlab(blockState: BlockState) : BlockWoodenSlab(blockState, BlockID.BAMBOO_DOUBLE_SLAB) {
    override fun getSlabName(): String {
        return "Bamboo"
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.BAMBOO_SLAB, CommonBlockProperties.MINECRAFT_VERTICAL_HALF)
    }
}