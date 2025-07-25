package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemTool

class BlockCrimsonDoubleSlab @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockDoubleSlabBase(blockstate) {
    override fun getSlabName(): String {
        return "Crimson"
    }

    override fun isCorrectTool(item: Item): Boolean {
        return true
    }

    override val hardness: Double
        get() = 2.0

    override val resistance: Double
        get() = 3.0

    override val toolType: Int
        get() = ItemTool.TYPE_AXE

    override val burnChance: Int
        get() = 0

    override val burnAbility: Int
        get() = 0

    override fun getSingleSlab(): BlockState {
        return BlockCrimsonSlab.properties.defaultState
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.CRIMSON_DOUBLE_SLAB, CommonBlockProperties.MINECRAFT_VERTICAL_HALF)
    }
}