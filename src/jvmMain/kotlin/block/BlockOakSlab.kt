package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockOakSlab(blockstate: BlockState) : BlockWoodenSlab(blockstate, BlockID.OAK_DOUBLE_SLAB) {
    override fun getSlabName(): String {
        return "Oak"
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.OAK_SLAB, CommonBlockProperties.MINECRAFT_VERTICAL_HALF)
    }
}