package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemSkeletonSkull

class BlockSkeletonSkull(blockState: BlockState) : BlockHead(blockState) {
    override val name: String
        get() = "Skeleton Skull"

    override fun getDrops(item: Item): Array<Item> {
        return arrayOf(
            this.toItem()
        )
    }

    override fun toItem(): Item {
        return ItemSkeletonSkull()
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.SKELETON_SKULL, CommonBlockProperties.FACING_DIRECTION)
    }
}
