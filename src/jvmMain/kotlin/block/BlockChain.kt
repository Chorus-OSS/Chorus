package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.math.BlockFace


class BlockChain(blockState: BlockState = properties.defaultState) : BlockTransparent(blockState) {
    override val name: String
        get() = "Chain"

    var pillarAxis: BlockFace.Axis
        get() = getPropertyValue(
            CommonBlockProperties.PILLAR_AXIS
        )
        set(axis) {
            setPropertyValue(
                CommonBlockProperties.PILLAR_AXIS,
                axis
            )
        }

    override fun place(
        item: Item?,
        block: Block,
        target: Block?,
        face: BlockFace,
        fx: Double,
        fy: Double,
        fz: Double,
        player: Player?
    ): Boolean {
        pillarAxis = face.axis
        return super.place(item, block, target, face, fx, fy, fz, player)
    }

    override val hardness: Double
        get() = 5.0

    override val waterloggingLevel: Int
        get() = 1

    override val resistance: Double
        get() = 6.0

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override var minX: Double
        get() = position.x + 7 / 16.0
        set(minX) {
            super.minX = minX
        }

    override var maxX: Double
        get() = position.x + 9 / 16.0
        set(maxX) {
            super.maxX = maxX
        }

    override var minZ: Double
        get() = position.z + 7 / 16.0
        set(minZ) {
            super.minZ = minZ
        }

    override var maxZ: Double
        get() = position.z + 9 / 16.0
        set(maxZ) {
            super.maxZ = maxZ
        }

    override val toolTier: Int
        get() = ItemTool.TIER_WOODEN

    override fun canHarvestWithHand(): Boolean {
        return false
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.CHAIN, CommonBlockProperties.PILLAR_AXIS)
    }
}
