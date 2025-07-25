package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockDarkOakSlab(blockstate: BlockState) : BlockWoodenSlab(blockstate, BlockID.DARK_OAK_DOUBLE_SLAB) {
    override fun getSlabName(): String {
        return "Dark Oak"
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.DARK_OAK_SLAB, CommonBlockProperties.MINECRAFT_VERTICAL_HALF)
    }
}