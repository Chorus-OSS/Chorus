package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.type.IntPropertyType
import org.chorus_oss.chorus.math.AxisAlignedBB

class BlockLightBlock @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockTransparent(blockstate) {
    override val name: String
        get() = "Light Block"

    override val lightLevel: Int
        get() = getPropertyValue<Int, IntPropertyType>(CommonBlockProperties.BLOCK_LIGHT_LEVEL)

    override val boundingBox: AxisAlignedBB?
        get() = null

    override val waterloggingLevel: Int
        get() = 2

    override fun canBeFlowedInto(): Boolean {
        return true
    }

    override fun canBeReplaced(): Boolean {
        return true
    }

    override fun canHarvestWithHand(): Boolean {
        return false
    }

    override val hardness: Double
        get() = 0.0

    override val resistance: Double
        get() = 0.0

    override fun canPassThrough(): Boolean {
        return true
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.LIGHT_BLOCK_0, CommonBlockProperties.BLOCK_LIGHT_LEVEL)
    }
}