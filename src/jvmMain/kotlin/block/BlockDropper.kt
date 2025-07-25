package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.blockentity.BlockEntityDropper
import org.chorus_oss.chorus.blockentity.BlockEntityID
import org.chorus_oss.chorus.dispenser.DispenseBehavior
import org.chorus_oss.chorus.dispenser.DropperDispenseBehavior
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemTool

class BlockDropper @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockDispenser(blockstate) {
    override val name: String
        get() = "Dropper"

    override fun getBlockEntityClass(): Class<out BlockEntityDropper?> {
        return BlockEntityDropper::class.java
    }

    override fun getBlockEntityType(): String {
        return BlockEntityID.DROPPER
    }

    override fun getDispenseBehavior(item: Item): DispenseBehavior {
        return DropperDispenseBehavior()
    }

    override val resistance: Double
        get() = 3.5

    override val hardness: Double
        get() = 3.5

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override val toolTier: Int
        get() = ItemTool.TIER_WOODEN

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(
                BlockID.DROPPER,
                CommonBlockProperties.FACING_DIRECTION,
                CommonBlockProperties.TRIGGERED_BIT
            )
    }
}
