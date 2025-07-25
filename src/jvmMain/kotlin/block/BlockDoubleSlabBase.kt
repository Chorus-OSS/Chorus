package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemTool

abstract class BlockDoubleSlabBase(blockState: BlockState) : BlockSolid(blockState) {
    override val name: String
        get() = "Double ${getSlabName()} Slab"

    abstract fun getSlabName(): String

    abstract fun getSingleSlab(): BlockState?

    override fun toItem(): Item {
        return get(getSingleSlab()).toItem()
    }

    override val hardness: Double
        //    public abstract int getToolType();
        get() = 2.0

    override val resistance: Double
        get() = (if (toolType == ItemTool.TYPE_PICKAXE) 6 else 3).toDouble()

    protected open fun isCorrectTool(item: Item): Boolean {
        return canHarvestWithHand() || canHarvest(item)
    }

    override fun getDrops(item: Item): Array<Item> {
        if (isCorrectTool(item)) {
            val slab = toItem()
            slab.setCount(2)
            return arrayOf(slab)
        } else {
            return Item.EMPTY_ARRAY
        }
    }
}
