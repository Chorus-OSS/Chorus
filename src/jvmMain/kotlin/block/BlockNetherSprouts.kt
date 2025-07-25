package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.item.Item

class BlockNetherSprouts @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockHanging(blockstate) {
    override val name: String
        get() = "Nether Sprouts Block"

    override fun getDrops(item: Item): Array<Item> {
        if (item.isShears) {
            return arrayOf(toItem())
        }
        return Item.EMPTY_ARRAY
    }

    override val burnChance: Int
        get() = 5

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.NETHER_SPROUTS)
    }
}