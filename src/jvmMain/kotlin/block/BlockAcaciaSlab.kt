package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockAcaciaSlab(blockstate: BlockState) : BlockWoodenSlab(blockstate, BlockID.ACACIA_DOUBLE_SLAB) {
    override fun getSlabName(): String {
        return "Acacia"
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.ACACIA_SLAB, CommonBlockProperties.MINECRAFT_VERTICAL_HALF)
    }
}