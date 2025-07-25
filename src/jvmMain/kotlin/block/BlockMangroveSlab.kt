package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockMangroveSlab @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockWoodenSlab(blockstate, BlockID.MANGROVE_DOUBLE_SLAB) {
    override val name: String
        get() = (if (isOnTop) "Upper " else "") + getSlabName() + " Wood Slab"

    override fun getSlabName(): String {
        return "Mangrove"
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.MANGROVE_SLAB, CommonBlockProperties.MINECRAFT_VERTICAL_HALF)
    }
}