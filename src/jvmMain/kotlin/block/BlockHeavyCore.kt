package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.math.AxisAlignedBB

class BlockHeavyCore(blockState: BlockState = properties.defaultState) : BlockFlowable(blockState) {
    override fun recalculateBoundingBox(): AxisAlignedBB {
        return this
    }

    override var minX: Double
        get() = position.x + 0.25
        set(minX) {
            super.minX = minX
        }

    override var minZ: Double
        get() = position.z + 0.25
        set(minZ) {
            super.minZ = minZ
        }

    override var maxX: Double
        get() = position.x + 0.75
        set(maxX) {
            super.maxX = maxX
        }

    override var maxY: Double
        get() = position.y + 0.50
        set(maxY) {
            super.maxY = maxY
        }

    override var maxZ: Double
        get() = position.z + 0.75
        set(maxZ) {
            super.maxZ = maxZ
        }

    override fun canPassThrough(): Boolean {
        return false
    }

    override val name: String
        get() = "Heavy Core"

    override val resistance: Double
        get() = 30.0

    override val hardness: Double
        get() = 10.0

    override val toolTier: Int
        get() = ItemTool.TIER_WOODEN

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override fun canHarvestWithHand(): Boolean {
        return false
    }

    override fun breaksWhenMoved(): Boolean {
        return false
    }

    override val waterloggingLevel: Int
        get() = 1

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.HEAVY_CORE)
    }
}