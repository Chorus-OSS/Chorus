package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockBirchSlab(blockstate: BlockState) : BlockWoodenSlab(blockstate, BlockID.BIRCH_DOUBLE_SLAB) {
    override fun getSlabName(): String {
        return "Birch"
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.BIRCH_SLAB, CommonBlockProperties.MINECRAFT_VERTICAL_HALF)
    }
}