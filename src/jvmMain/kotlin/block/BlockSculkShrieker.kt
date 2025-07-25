package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.blockentity.BlockEntityID
import org.chorus_oss.chorus.blockentity.BlockEntitySculkShrieker
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.math.AxisAlignedBB

class BlockSculkShrieker @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockFlowable(blockstate), BlockEntityHolder<BlockEntitySculkShrieker> {
    override val name: String
        get() = "Sculk Shrieker"

    override fun canBePulled(): Boolean {
        return false
    }

    override fun canBePushed(): Boolean {
        return false
    }

    override val resistance: Double
        get() = 3.0

    override val hardness: Double
        get() = 3.0

    override val toolType: Int
        get() = ItemTool.TYPE_HOE

    override fun getBlockEntityClass() = BlockEntitySculkShrieker::class.java

    override fun getBlockEntityType() = BlockEntityID.SCULK_SHRIEKER

    override fun canPassThrough(): Boolean {
        return false
    }

    override fun breaksWhenMoved(): Boolean {
        return false
    }

    override fun canBeFlowedInto(): Boolean {
        return false
    }

    override fun recalculateBoundingBox(): AxisAlignedBB {
        return this
    }

    override val waterloggingLevel: Int
        get() = 1

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.SCULK_SHRIEKER, CommonBlockProperties.ACTIVE, CommonBlockProperties.CAN_SUMMON)
    }
}
