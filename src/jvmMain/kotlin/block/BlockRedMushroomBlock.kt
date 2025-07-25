package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.item.Item
import java.util.concurrent.ThreadLocalRandom

class BlockRedMushroomBlock @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockMushroomBlock(blockstate) {
    override fun getDrops(item: Item): Array<Item> {
        return if (ThreadLocalRandom.current().nextInt(1, 20) == 1) {
            arrayOf(
                Item.get(BlockID.RED_MUSHROOM_BLOCK)
            )
        } else {
            Item.EMPTY_ARRAY
        }
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.RED_MUSHROOM_BLOCK, CommonBlockProperties.HUGE_MUSHROOM_BITS)
    }
}