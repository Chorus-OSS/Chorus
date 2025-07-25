package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.item.ItemTool

class BlockRawGoldBlock @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockRaw(blockstate) {
    override val name: String
        get() = "Block of Raw Gold"

    override val toolTier: Int
        get() = ItemTool.TIER_IRON

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.RAW_GOLD_BLOCK)
    }
}