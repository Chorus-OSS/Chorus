package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockJungleSlab(blockstate: BlockState) : BlockWoodenSlab(blockstate, BlockID.JUNGLE_DOUBLE_SLAB) {
    override fun getSlabName(): String {
        return "Jungle"
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.JUNGLE_SLAB, CommonBlockProperties.MINECRAFT_VERTICAL_HALF)
    }
}