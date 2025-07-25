package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.Item.Companion.get
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.item.ItemTool

open class BlockBookshelf(blockState: BlockState = properties.defaultState) : BlockSolid(blockState) {
    override val name: String
        get() = "Bookshelf"

    override val hardness: Double
        get() = 1.5

    override val resistance: Double
        get() = 7.5

    override val toolType: Int
        get() = ItemTool.TYPE_AXE

    override val burnChance: Int
        get() = 30

    override val burnAbility: Int
        get() = 20

    override fun getDrops(item: Item): Array<Item> {
        return arrayOf(
            get(ItemID.BOOK, 0, 3)
        )
    }

    override fun canSilkTouch(): Boolean {
        return true
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.BOOKSHELF)
    }
}
