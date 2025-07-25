package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.blockentity.BlockEntityEndGateway
import org.chorus_oss.chorus.blockentity.BlockEntityID
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.Vector3

class BlockEndGateway(blockState: BlockState = properties.defaultState) : BlockSolid(blockState),
    BlockEntityHolder<BlockEntityEndGateway> {
    override val name: String
        get() = "End Gateway"

    override fun getBlockEntityClass() = BlockEntityEndGateway::class.java

    override fun getBlockEntityType(): String = BlockEntityID.END_GATEWAY

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
        return BlockEntityHolder.setBlockAndCreateEntity(this) != null
    }

    override fun canPassThrough(): Boolean {

        return if (level.dimension != Level.DIMENSION_THE_END) {
            false
        } else {
            !getOrCreateBlockEntity().isTeleportCooldown()
        }
    }

    override fun isBreakable(vector: Vector3, layer: Int, face: BlockFace?, item: Item?, player: Player?): Boolean {
        return false
    }

    override val hardness: Double
        get() = -1.0

    override val resistance: Double
        get() = 3600000.0

    override val lightLevel: Int
        get() = 15

    override fun hasEntityCollision(): Boolean {
        return true
    }

    override fun toItem(): Item {
        return Item.AIR
    }

    override fun canBePushed(): Boolean {
        return false
    }

    override fun canBePulled(): Boolean {
        return false
    }

    override fun onEntityCollide(entity: Entity) {

        if (level.dimension != Level.DIMENSION_THE_END) {
            return
        }

        val endGateway = getOrCreateBlockEntity()

        if (!endGateway.isTeleportCooldown()) {
            endGateway.teleportEntity(entity)
        }
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.END_GATEWAY)
    }
}
