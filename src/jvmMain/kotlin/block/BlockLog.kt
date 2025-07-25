package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.math.BlockFace

abstract class BlockLog(blockState: BlockState) : BlockSolid(blockState), IBlockWood {
    abstract override fun getStrippedState(): BlockState

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

    override fun canBeActivated(): Boolean {
        return true
    }

    override fun onActivate(
        item: Item,
        player: Player?,
        blockFace: BlockFace,
        fx: Float,
        fy: Float,
        fz: Float
    ): Boolean {
        if (item.isAxe) {
            val strippedBlock = get(getStrippedState())
            strippedBlock.setPropertyValue(
                CommonBlockProperties.PILLAR_AXIS,
                pillarAxis
            )
            item.useOn(this)
            level.setBlock(this.position, strippedBlock, true, true)
            return true
        }
        return false
    }

    override val toolType: Int
        get() = ItemTool.TYPE_AXE

    override val resistance: Double
        get() = 2.0

    override val hardness: Double
        get() = 2.0

    override val burnChance: Int
        get() = 5

    override val burnAbility: Int
        get() = 10
}
