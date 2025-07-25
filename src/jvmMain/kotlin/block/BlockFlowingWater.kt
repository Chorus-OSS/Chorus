package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.math.BlockFace

open class BlockFlowingWater @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockLiquid(blockstate) {
    override val name: String
        get() = "Flowing Water"

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
        val ret = level.setBlock(this.position, this, true, false)
        level.scheduleUpdate(this, this.tickRate())
        return ret
    }

    override fun afterRemoval(newBlock: Block, update: Boolean) {
        if (!update) {
            return
        }

        val newId = newBlock.id
        if (newId == BlockID.FLOWING_WATER || newId == BlockID.WATER) {
            return
        }

        val up = up(1, 0)
        for (diagonalFace in BlockFace.Plane.HORIZONTAL_FACES) {
            val diagonal = up.getSide(diagonalFace)
            if (diagonal.id == BlockID.REEDS) {
                diagonal.onUpdate(Level.BLOCK_UPDATE_SCHEDULED)
            }
        }
    }

    override fun getLiquidWithNewDepth(depth: Int): BlockLiquid {
        return BlockFlowingWater(
            blockState.setPropertyValue(
                Companion.properties,
                CommonBlockProperties.LIQUID_DEPTH.createValue(depth)
            )
        )
    }

    override fun onEntityCollide(entity: Entity) {
        super.onEntityCollide(entity)

        if (entity.fireTicks > 0) {
            entity.extinguish()
        }
    }

    override fun tickRate(): Int {
        return 5
    }

    override fun usesWaterLogging(): Boolean {
        return true
    }

    override val passableBlockFrictionFactor: Double
        get() = 0.5

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.FLOWING_WATER, CommonBlockProperties.LIQUID_DEPTH)
    }
}