package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.math.AxisAlignedBB
import org.chorus_oss.chorus.math.SimpleAxisAlignedBB
import org.chorus_oss.chorus.math.VectorMath.calculateFace

abstract class BlockFence(blockState: BlockState) : BlockTransparent(blockState), BlockConnectable {
    override val hardness: Double
        get() = 2.0

    override val waterloggingLevel: Int
        get() = 1

    override val resistance: Double
        get() = 3.0

    override val toolType: Int
        get() = ItemTool.TYPE_AXE

    override fun recalculateBoundingBox(): AxisAlignedBB? {
        val north = this.canConnect(this.north())
        val south = this.canConnect(this.south())
        val west = this.canConnect(this.west())
        val east = this.canConnect(this.east())
        val n = if (north) 0.0 else 0.375
        val s = if (south) 1.0 else 0.625
        val w = if (west) 0.0 else 0.375
        val e = if (east) 1.0 else 0.625
        return SimpleAxisAlignedBB(
            position.x + w,
            position.y,
            position.z + n,
            position.x + e,
            position.y + 1.5,
            position.z + s
        )
    }

    override val burnChance: Int
        get() = 5

    override val burnAbility: Int
        get() = 20

    override fun canConnect(block: Block?): Boolean {
        if (block == null) return false
        if (block is BlockFence) {
            if (block.id == BlockID.NETHER_BRICK_FENCE || this.id == BlockID.NETHER_BRICK_FENCE) {
                return block.id == this.id
            }
            return true
        }
        if (block is BlockTrapdoor) {
            return block.isOpen && block.blockFace == calculateFace(this.position, block.position)
        }
        return block is BlockFenceGate || block.isSolid && !block.isTransparent
    }
}
