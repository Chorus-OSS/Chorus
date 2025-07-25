package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.type.IntPropertyType
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.math.BlockFace

class BlockResinClump @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockLichen(blockstate) {
    override fun canHarvestWithHand(): Boolean {
        return true
    }

    override val toolType: Int
        get() = ItemTool.TYPE_NONE

    override fun getDrops(item: Item): Array<Item> {
        val drop = toItem()
        drop.setCount(growthSides.size)
        return arrayOf(drop)
    }

    override fun witherAtSide(side: BlockFace) {
        if (isGrowthToSide(side)) {
            setPropertyValue<Int, IntPropertyType>(
                CommonBlockProperties.MULTI_FACE_DIRECTION_BITS,
                getPropertyValue<Int, IntPropertyType>(CommonBlockProperties.MULTI_FACE_DIRECTION_BITS) xor (1 shl side.indexDUSWNE)
            )
            level.setBlock(this.position, this, true, true)
            level.dropItem(this.position, toItem())
        }
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.RESIN_CLUMP, CommonBlockProperties.MULTI_FACE_DIRECTION_BITS)
    }
}