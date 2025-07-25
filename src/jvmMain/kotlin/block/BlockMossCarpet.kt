package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.item.Item

open class BlockMossCarpet @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockCarpet(blockstate) {
    override val name: String
        get() = "Moss Carpet"

    override val resistance: Double
        get() = 0.1

    override fun getDrops(item: Item): Array<Item> {
        return arrayOf(toItem())
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.MOSS_CARPET)
    }
}
