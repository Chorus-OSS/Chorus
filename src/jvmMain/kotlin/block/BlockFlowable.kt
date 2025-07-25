package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.math.AxisAlignedBB


abstract class BlockFlowable(blockState: BlockState) : BlockTransparent(blockState) {
    override fun canBeFlowedInto(): Boolean {
        return true
    }

    override fun canPassThrough(): Boolean {
        return true
    }

    override val hardness: Double
        get() = 0.0

    override val resistance: Double
        get() = 0.0

    override val isSolid: Boolean
        get() = false

    override fun breaksWhenMoved(): Boolean {
        return true
    }

    override fun sticksToPiston(): Boolean {
        return false
    }

    override fun recalculateBoundingBox(): AxisAlignedBB? {
        return null
    }
}
