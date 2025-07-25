package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemHead
import org.chorus_oss.chorus.item.ItemPiglinHead

class BlockPiglinHead(blockState: BlockState) : BlockHead(blockState), ItemHead {
    override val name: String
        get() = "Piglin Head"

    override fun getDrops(item: Item): Array<Item> {
        return arrayOf(
            this.toItem()
        )
    }

    override fun toItem(): Item {
        return ItemPiglinHead()
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.PIGLIN_HEAD, CommonBlockProperties.FACING_DIRECTION)
    }
}
