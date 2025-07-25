package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockDarkOakDoubleSlab(blockstate: BlockState) : BlockDoubleWoodenSlab(blockstate) {

    override fun getSlabName(): String {
        return "Dark Oak"
    }

    override fun getSingleSlab(): BlockState {
        return BlockDarkOakSlab.properties.defaultState
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.DARK_OAK_DOUBLE_SLAB, CommonBlockProperties.MINECRAFT_VERTICAL_HALF)
    }
}