package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockPaleOakSlab(blockstate: BlockState) : BlockWoodenSlab(blockstate, BlockID.PALE_OAK_DOUBLE_SLAB) {
    override fun getSlabName(): String {
        return "Pale Oak"
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.PALE_OAK_SLAB, CommonBlockProperties.MINECRAFT_VERTICAL_HALF)
    }
}