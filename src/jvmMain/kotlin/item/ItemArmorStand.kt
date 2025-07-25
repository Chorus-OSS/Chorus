package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.Entity.Companion.createEntity
import org.chorus_oss.chorus.entity.Entity.Companion.getDefaultNBT
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.mob.EntityArmorStand
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.CompassRoseDirection
import org.chorus_oss.chorus.math.SimpleAxisAlignedBB
import org.chorus_oss.chorus.math.Vector3

class ItemArmorStand : Item(ItemID.Companion.ARMOR_STAND) {
    override fun canBeActivated(): Boolean {
        return true
    }

    override val maxStackSize: Int
        get() = 64

    override fun onActivate(
        level: Level,
        player: Player,
        block: Block,
        target: Block,
        face: BlockFace,
        fx: Double,
        fy: Double,
        fz: Double
    ): Boolean {
        if (player.isAdventure) {
            return false
        }

        val chunk = block.chunk

        if (!block.canBeReplaced() || !block.up().canBeReplaced()) {
            return false
        }

        for (collidingEntity in level.getCollidingEntities(
            SimpleAxisAlignedBB(
                block.position.x,
                block.position.y,
                block.position.z,
                block.position.x + 1,
                block.position.y + 1,
                block.position.z + 1
            )
        )) {
            if (collidingEntity is EntityArmorStand) {
                return false
            }
        }

        val direction = CompassRoseDirection.getClosestFromYaw(
            player.rotation.yaw,
            CompassRoseDirection.Precision.PRIMARY_INTER_CARDINAL
        ).oppositeFace
        val nbt = getDefaultNBT(block.position.add(0.5, 0.0, 0.5), Vector3(), direction.yaw, 0f)
        if (this.hasCustomName()) {
            nbt.putString("CustomName", this.customName)
        }

        if (!removeForPlacement(block) || !removeForPlacement(block.up())) {
            return false
        }

        val entity: Entity = createEntity(EntityID.ARMOR_STAND, chunk, nbt) ?: return false

        if (!player.isCreative) {
            player.inventory.decreaseCount(player.inventory.heldItemIndex)
        }

        entity.spawnToAll()
        player.level!!.addSound(entity.position, Sound.MOB_ARMOR_STAND_PLACE)
        return true
    }

    /**
     * @param block The block which is in the same space as the armor stand.
     * @return `true` if the armor stand entity can be placed
     */
    protected fun removeForPlacement(block: Block): Boolean {
        return when (block.id) {
            BlockID.AIR -> true
            BlockID.SNOW_LAYER -> block.canBeReplaced()
            else -> block.level.setBlock(block.position, Block.get(BlockID.AIR))
        }
    }
}
