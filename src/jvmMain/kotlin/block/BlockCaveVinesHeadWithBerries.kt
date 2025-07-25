package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID

class BlockCaveVinesHeadWithBerries @JvmOverloads constructor(blockState: BlockState = properties.defaultState) :
    BlockCaveVines(blockState) {
    override val name: String
        get() = "Cave Vines Head With Berries"

    override val isTransparent: Boolean
        get() = true

    override val lightLevel: Int
        get() = 14

    override fun getDrops(item: Item): Array<Item> {
        return arrayOf(Item.get(ItemID.GLOW_BERRIES))
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.CAVE_VINES_HEAD_WITH_BERRIES, CommonBlockProperties.GROWING_PLANT_AGE)
    }
}
