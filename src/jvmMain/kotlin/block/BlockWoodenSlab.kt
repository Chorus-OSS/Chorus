package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.item.ItemTool

abstract class BlockWoodenSlab : BlockSlab {
    constructor(blockState: BlockState, doubleSlab: BlockState?) : super(blockState, doubleSlab)

    constructor(blockState: BlockState, doubleSlab: String) : super(blockState, doubleSlab)

    override val name: String
        get() = (if (isOnTop) "Upper " else "") + getSlabName() + " Wood Slab"

    override fun isSameType(slab: BlockSlab): Boolean {
        return slab.id == id
    }

    override val burnChance: Int
        get() = 5

    override val burnAbility: Int
        get() = 20

    override val toolType: Int
        get() = ItemTool.TYPE_AXE

    override fun canHarvestWithHand(): Boolean {
        return true
    }

    override val toolTier: Int
        get() = ItemTool.TYPE_NONE
}