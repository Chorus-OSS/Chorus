package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemPlayerHead

class BlockPlayerHead(blockState: BlockState) : BlockHead(blockState) {
    override val name: String
        get() = "Player Head"

    override fun getDrops(item: Item): Array<Item> {
        return arrayOf(
            this.toItem()
        )
    }

    override fun toItem(): Item {
        return ItemPlayerHead()
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.PLAYER_HEAD, CommonBlockProperties.FACING_DIRECTION)
    }
}
