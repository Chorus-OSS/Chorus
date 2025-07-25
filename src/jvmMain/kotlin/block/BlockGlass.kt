package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.item.Item

open class BlockGlass(blockState: BlockState = properties.defaultState) : BlockTransparent(blockState) {
    override val name: String
        get() = "Glass"

    override val resistance: Double
        get() = 0.3

    override val hardness: Double
        get() = 0.3

    override fun getDrops(item: Item): Array<Item> {
        return Item.EMPTY_ARRAY
    }

    override fun canSilkTouch(): Boolean {
        return true
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.GLASS)
    }
}
