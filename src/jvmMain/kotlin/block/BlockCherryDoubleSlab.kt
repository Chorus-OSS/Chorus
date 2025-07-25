package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.item.ItemTool

class BlockCherryDoubleSlab @JvmOverloads constructor(blockState: BlockState = properties.defaultState) :
    BlockDoubleSlabBase(blockState) {
    override fun getSlabName(): String {
        return "Cherry"
    }

    override val hardness: Double
        get() = 2.0

    override val resistance: Double
        get() = 15.0

    override val burnChance: Int
        get() = 5

    override val burnAbility: Int
        get() = 20

    override val toolType: Int
        get() = ItemTool.TYPE_AXE

    override fun getSingleSlab(): BlockState {
        return BlockCherrySlab.properties.defaultState
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.CHERRY_DOUBLE_SLAB, CommonBlockProperties.MINECRAFT_VERTICAL_HALF)
    }
}