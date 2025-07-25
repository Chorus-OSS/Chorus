package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockOakDoubleSlab(blockstate: BlockState) : BlockDoubleWoodenSlab(blockstate) {
    override fun getSlabName() = "Oak"

    override fun getSingleSlab() = BlockOakSlab.properties.defaultState

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.OAK_DOUBLE_SLAB, CommonBlockProperties.MINECRAFT_VERTICAL_HALF)
    }
}