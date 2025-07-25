package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemTool

open class BlockPackedIce @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockIce(blockstate) {
    override val name: String
        get() = "Packed Ice"

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override fun onUpdate(type: Int): Int {
        return 0 //not being melted
    }

    override fun canHarvestWithHand(): Boolean {
        return true
    }

    override fun onBreak(item: Item?): Boolean {
        level.setBlock(this.position, get(BlockID.AIR), true) //no water
        return true
    }

    override fun canSilkTouch(): Boolean {
        return true
    }

    override val isTransparent: Boolean
        get() = false

    override val burnChance: Int
        get() = 0

    override val lightFilter: Int
        get() = 15

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.PACKED_ICE)

    }
}