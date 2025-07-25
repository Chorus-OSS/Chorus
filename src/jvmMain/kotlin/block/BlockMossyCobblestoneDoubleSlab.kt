package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.item.ItemTool

class BlockMossyCobblestoneDoubleSlab @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockDoubleSlabBase(blockstate) {
    override fun getSlabName() = "Mossy Cobblestone"

    override fun getSingleSlab() = BlockMossyCobblestoneSlab.properties.defaultState

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.MOSSY_COBBLESTONE_DOUBLE_SLAB, CommonBlockProperties.MINECRAFT_VERTICAL_HALF)

    }
}