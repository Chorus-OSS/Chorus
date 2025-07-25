package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockAcaciaDoubleSlab(blockstate: BlockState) : BlockDoubleWoodenSlab(blockstate) {
    override fun getSlabName(): String {
        return "Acacia"
    }

    override fun getSingleSlab(): BlockState {
        return Companion.properties.defaultState
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.ACACIA_DOUBLE_SLAB, CommonBlockProperties.MINECRAFT_VERTICAL_HALF)
    }
}