package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemBlock
import org.chorus_oss.chorus.item.ItemTool

class BlockIronBars @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockThin(blockstate) {
    override val name: String
        get() = "Iron Bars"

    override val hardness: Double
        get() = 5.0

    override val waterloggingLevel: Int
        get() = 1

    override val resistance: Double
        get() = 10.0

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override fun toItem(): Item {
        return ItemBlock(this.blockState, name, 0)
    }

    override val toolTier: Int
        get() = ItemTool.TIER_WOODEN

    override fun canHarvestWithHand(): Boolean {
        return false
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.IRON_BARS)
    }
}
