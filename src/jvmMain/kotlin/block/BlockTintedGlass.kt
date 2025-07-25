package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.item.Item

class BlockTintedGlass @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockGlass(blockstate) {
    override val name: String
        get() = "Tinted Glass"

    override fun getDrops(item: Item): Array<Item> {
        return arrayOf(toItem())
    }

    override fun canSilkTouch(): Boolean {
        return false
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.TINTED_GLASS)

    }
}