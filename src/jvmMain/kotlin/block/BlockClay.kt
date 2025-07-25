package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemClayBall
import org.chorus_oss.chorus.item.ItemTool

class BlockClay(blockState: BlockState = properties.defaultState) : BlockSolid(blockState), Natural {
    override val hardness: Double
        get() = 0.6

    override val resistance: Double
        get() = 3.0

    override val toolType: Int
        get() = ItemTool.TYPE_SHOVEL

    override val name: String
        get() = "Clay Block"

    override fun getDrops(item: Item): Array<Item> {
        val clayBall: Item = ItemClayBall()
        clayBall.setCount(4)
        return arrayOf(
            clayBall
        )
    }

    override fun canSilkTouch(): Boolean {
        return true
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.CLAY)
    }
}
