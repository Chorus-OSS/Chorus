package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.item.ItemTool

open class BlockGoldOre @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockOre(blockstate) {
    override val name: String
        get() = "Gold Ore"

    override val rawMaterial: String?
        get() = ItemID.RAW_GOLD

    override val toolTier: Int
        get() = ItemTool.TIER_IRON

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.GOLD_ORE)
    }
}