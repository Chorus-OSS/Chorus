package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.math.BlockFace

class BlockPurpurPillar @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockSolid(blockstate) {
    override val name: String
        get() = "Purpur Pillar"

    override val hardness: Double
        get() = 1.5

    override val resistance: Double
        get() = 30.0

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

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
        this.pillarAxis = face.axis
        level.setBlock(block.position, this, true, true)
        return true
    }

    override val toolTier: Int
        get() = ItemTool.TIER_WOODEN

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


    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.PURPUR_PILLAR, CommonBlockProperties.PILLAR_AXIS)
    }
}
