package org.chorus_oss.chorus.entity.projectile.throwable

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.mob.monster.EntityEndermite
import org.chorus_oss.chorus.event.entity.EntityDamageByEntityEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent.DamageCause
import org.chorus_oss.chorus.event.player.PlayerTeleportEvent.TeleportCause
import org.chorus_oss.chorus.level.GameRule
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.FloatTag
import org.chorus_oss.chorus.nbt.tag.ListTag
import org.chorus_oss.protocol.packets.LevelEventPacket
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.floor

class EntityEnderPearl @JvmOverloads constructor(chunk: IChunk?, nbt: CompoundTag, shootingEntity: Entity? = null) :
    EntityThrowable(chunk, nbt, shootingEntity) {

    override fun getEntityIdentifier(): String {
        return EntityID.ENDER_PEARL
    }

    override fun getWidth(): Float {
        return 0.25f
    }

    override fun getLength(): Float {
        return 0.25f
    }

    override fun getHeight(): Float {
        return 0.25f
    }

    override fun getGravity(): Float {
        return 0.03f
    }

    override fun getDrag(): Float {
        return 0.01f
    }

    override fun onUpdate(currentTick: Int): Boolean {
        if (this.closed) {
            return false
        }
        var hasUpdate: Boolean = super.onUpdate(currentTick)

        if (this.isCollided && shootingEntity is Player) {
            var portal: Boolean = false
            for (collided: Block in getCollisionBlocks()!!) {
                if (collided.id === BlockID.PORTAL) {
                    portal = true
                }
            }
            if (!portal) {
                teleport(shootingEntity as Player)
            }
        }

        if (this.age > 1200 || this.isCollided) {
            this.kill()
            hasUpdate = true
        }

        return hasUpdate
    }

    override fun onCollideWithEntity(entity: Entity) {
        if (shootingEntity is Player) {
            teleport(shootingEntity as Player)
        }
        super.onCollideWithEntity(entity)
    }

    private fun teleport(player: Player) {
        if (this.level != shootingEntity!!.level) {
            return
        }

        level!!.addLevelEvent(
            player.position.add(0.5, 0.5, 0.5),
            LevelEventPacket.SOUND_TELEPORT_ENDERPEARL
        )
        val destination = Vector3(
            floor(position.x) + 0.5,
            position.y + 1, floor(position.z) + 0.5
        )
        player.teleport(destination, TeleportCause.ENDER_PEARL)
        if ((player.gamemode and 0x01) == 0) {
            player.attack(EntityDamageByEntityEvent(this, player, DamageCause.PROJECTILE, 5f, 0f))
        }
        level!!.addLevelEvent(this.position, LevelEventPacket.PARTICLE_TELEPORT)
        level!!.addLevelEvent(
            player.position.add(0.5, 0.5, 0.5),
            LevelEventPacket.SOUND_TELEPORT_ENDERPEARL
        )
        if (level!!.gameRules.getBoolean(GameRule.DO_MOB_SPAWNING)) {
            if (ThreadLocalRandom.current().nextInt(1, 20) == 1) {
                val endermite: EntityEndermite? = createEntity(
                    EntityID.ENDERMITE,
                    level!!.getChunk(destination.floorX shr 4, destination.floorZ shr 4), CompoundTag()
                        .putList(
                            "Pos", ListTag<FloatTag>()
                                .add(FloatTag(destination.x + 0.5))
                                .add(FloatTag(destination.y + 0.0625))
                                .add(FloatTag(destination.z + 0.5))
                        )
                        .putList(
                            "Motion", ListTag<FloatTag>()
                                .add(FloatTag(0f))
                                .add(FloatTag(0f))
                                .add(FloatTag(0f))
                        )
                        .putList(
                            "Rotation", ListTag<FloatTag>()
                                .add(FloatTag(0f))
                                .add(FloatTag(0f))
                        )
                ) as EntityEndermite?
                endermite!!.spawnToAll()
            }
        }
    }

    override fun getOriginalName(): String {
        return "Ender Pearl"
    }
}
