package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemTool

class BlockInfestedStoneBricks @JvmOverloads constructor(blockState: BlockState = properties.defaultState) :
    BlockSolid(blockState) {
    override val name: String
        get() = "Infested Stone Bricks"

    override val hardness: Double
        get() = 0.75

    override val resistance: Double
        get() = 0.75

    override fun getDrops(item: Item): Array<Item> {
        return Item.EMPTY_ARRAY
    }

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.INFESTED_STONE_BRICKS)
    }
}
