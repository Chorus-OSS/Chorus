package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockPetrifiedOakDoubleSlab(blockstate: BlockState) : BlockDoubleWoodenSlab(blockstate) {
    override fun getSlabName() = "Petrified Oak"

    override fun getSingleSlab() = BlockPetrifiedOakSlab.properties.defaultState

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.PETRIFIED_OAK_DOUBLE_SLAB, CommonBlockProperties.MINECRAFT_VERTICAL_HALF)
    }
}