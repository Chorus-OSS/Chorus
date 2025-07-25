package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.item.ItemTool
import java.util.concurrent.ThreadLocalRandom

open class BlockCoalOre @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockOre(blockstate) {
    override val name: String
        get() = "Coal Ore"

    override val rawMaterial: String?
        get() = ItemID.COAL

    override val toolTier: Int
        get() = ItemTool.TIER_WOODEN

    override val dropExp: Int
        get() = ThreadLocalRandom.current().nextInt(3)

    override val resistance: Double
        get() = 3.0

    override val hardness: Double
        get() = 3.0

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.COAL_ORE)
    }
}