package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemDragonHead

class BlockDragonHead(blockState: BlockState) : BlockHead(blockState) {
    override val name: String
        get() = "Dragon Head"

    override fun getDrops(item: Item): Array<Item> {
        return arrayOf(
            this.toItem()
        )
    }

    override fun toItem(): Item {
        return ItemDragonHead()
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.DRAGON_HEAD, CommonBlockProperties.FACING_DIRECTION)
    }
}
