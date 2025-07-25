package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.math.BlockFace

class BlockMuddyMangroveRoots(blockState: BlockState = properties.defaultState) : BlockSolid(blockState), Natural {
    override val name: String
        get() = "Muddy Mangrove Roots"

    override val hardness: Double
        get() = 0.7

    override val resistance: Double
        get() = 0.7

    override val toolTier: Int
        get() = ItemTool.TYPE_SHOVEL

    override val isTransparent: Boolean
        get() = true

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
        level.setBlock(block.position, this, true, true)
        return true
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.MUDDY_MANGROVE_ROOTS, CommonBlockProperties.PILLAR_AXIS)
    }
}
