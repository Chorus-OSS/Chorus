package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.item.ItemTool

open class BlockCopperOre @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockOre(blockstate) {
    override val name: String
        get() = "Copper Ore"

    override val toolTier: Int
        get() = ItemTool.TIER_STONE

    override val rawMaterial: String?
        get() = ItemID.RAW_COPPER

    override val dropMultiplier: Float
        get() = 3f

    override val hardness: Double
        get() = 3.0

    override val resistance: Double
        get() = 3.0

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.COPPER_ORE)
    }
}