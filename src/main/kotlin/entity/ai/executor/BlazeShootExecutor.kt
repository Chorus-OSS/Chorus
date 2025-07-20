package org.chorus_oss.chorus.entity.ai.executor

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.EntityLiving
import org.chorus_oss.chorus.entity.ai.memory.NullableMemoryType
import org.chorus_oss.chorus.entity.data.EntityDataTypes
import org.chorus_oss.chorus.entity.data.EntityFlag
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.entity.projectile.EntityProjectile
import org.chorus_oss.chorus.entity.projectile.EntitySmallFireball
import org.chorus_oss.chorus.event.entity.ProjectileLaunchEvent
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.FloatTag
import org.chorus_oss.chorus.nbt.tag.ListTag
import org.chorus_oss.chorus.plugin.InternalPlugin
import org.chorus_oss.protocol.packets.LevelEventPacket
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class BlazeShootExecutor(
    protected var memory: NullableMemoryType<out Entity>,
    protected var speed: Float,
    maxShootDistance: Int,
    protected var clearDataWhenLose: Boolean,
    protected val coolDownTick: Int,
    protected val fireTick: Int
) :
    EntityControl, IBehaviorExecutor {
    protected var maxShootDistanceSquared: Int = maxShootDistance * maxShootDistance

    /**
     *
     *
     * Used to specify a specific attack target.
     */
    protected var target: Entity? = null

    private var tick1 = 0 //control the coolDownTick
    private var tick2 = 0 //control the pullBowTick

    override fun execute(entity: EntityMob): Boolean {
        if (tick2 == 0) {
            tick1++
        }
        if (!entity.isEnablePitch) entity.isEnablePitch = true
        if (entity.behaviorGroup.memoryStorage.isEmpty(memory)) return false
        val newTarget = entity.behaviorGroup.memoryStorage.get(memory) ?: return false
        if (this.target == null) target = newTarget

        if (!target!!.isAlive()) return false
        else if (target is Player) {
            val player = target as Player
            if (player.isCreative || player.isSpectator || !player.isOnline || (entity.level!!.name != player.level!!.name)) {
                return false
            }
        }

        if (target!!.locator != newTarget.locator) {
            target = newTarget
        }

        if (entity.movementSpeed != speed) entity.setMovementSpeedF(speed)
        val clone = target!!.transform

        if (entity.position.distanceSquared(target!!.position) > maxShootDistanceSquared) {
            setRouteTarget(entity, clone.position)
        } else {
            setRouteTarget(entity, null)
        }
        setLookTarget(entity, clone.position)

        if (tick2 == 0 && tick1 > coolDownTick) {
            if (entity.position.distanceSquared(target!!.position) <= maxShootDistanceSquared) {
                this.tick1 = 0
                tick2++
                startOnFire(entity)
            }
        } else if (tick2 != 0) {
            tick2++
            if (tick2 > fireTick) {
                for (i in 0..2) {
                    entity.level!!.scheduler.scheduleDelayedTask(
                        InternalPlugin.INSTANCE,
                        { shootFireball(entity) }, i * 6
                    )
                }
                entity.level!!.scheduler.scheduleDelayedTask(
                    InternalPlugin.INSTANCE,
                    { stopOnFire(entity) }, 20
                )
                tick2 = 0
                return target!!.health != 0f
            }
        }
        return true
    }

    override fun onStop(entity: EntityMob) {
        removeRouteTarget(entity)
        removeLookTarget(entity)
        entity.setMovementSpeedF(EntityLiving.DEFAULT_SPEED)
        if (clearDataWhenLose) {
            entity.behaviorGroup.memoryStorage.clear(memory)
        }
        entity.isEnablePitch = (false)
        stopOnFire(entity)
        this.target = null
    }

    override fun onInterrupt(entity: EntityMob) {
        removeRouteTarget(entity)
        removeLookTarget(entity)
        entity.setMovementSpeedF(EntityLiving.DEFAULT_SPEED)
        if (clearDataWhenLose) {
            entity.behaviorGroup.memoryStorage.clear(memory)
        }
        entity.isEnablePitch = false
        stopOnFire(entity)
        this.target = null
    }

    protected fun shootFireball(entity: EntityMob) {
        val fireballTransform = entity.transform
        val directionVector =
            entity.getDirectionVector().multiply((1 + ThreadLocalRandom.current().nextFloat(0.2f)).toDouble())
        fireballTransform.setY(entity.position.y + entity.getEyeHeight() + directionVector.y)
        val nbt = CompoundTag()
            .putList(
                "Pos", ListTag<FloatTag>()
                    .add(FloatTag(fireballTransform.position.x))
                    .add(FloatTag(fireballTransform.position.y))
                    .add(FloatTag(fireballTransform.position.z))
            )
            .putList(
                "Motion", ListTag<FloatTag>()
                    .add(FloatTag(-sin(entity.headYaw / 180 * Math.PI) * cos(entity.rotation.pitch / 180 * Math.PI)))
                    .add(FloatTag(-sin(entity.rotation.pitch / 180 * Math.PI)))
                    .add(FloatTag(cos(entity.headYaw / 180 * Math.PI) * cos(entity.rotation.pitch / 180 * Math.PI)))
            )
            .putList(
                "Rotation", ListTag<FloatTag>()
                    .add(FloatTag((if (entity.headYaw > 180) 360 else 0).toFloat() - entity.headYaw))
                    .add(FloatTag(-entity.rotation.pitch.toFloat()))
            )
            .putDouble("damage", 2.0)

        val p = 1.0
        min((p * p + p * 2) / 3, 1.0) * 3

        val projectile: Entity = Entity.createEntity(
            EntityID.SMALL_FIREBALL,
            entity.level!!.getChunk(entity.position.chunkX, entity.position.chunkZ),
            nbt
        )
            ?: return

        if (projectile is EntitySmallFireball) {
            projectile.shootingEntity = entity
        }

        val projectev = ProjectileLaunchEvent(projectile as EntityProjectile, entity)
        Server.instance.pluginManager.callEvent(projectev)
        if (projectev.cancelled) {
            projectile.kill()
        } else {
            projectile.spawnToAll()
            entity.level!!.addLevelEvent(entity.position, LevelEventPacket.SOUND_BLAZE_FIREBALL)
        }
    }

    private fun startOnFire(entity: Entity) {
        entity.setDataProperty(EntityDataTypes.TARGET_EID, target!!.getRuntimeID())
        entity.setDataFlag(EntityFlag.CHARGED, true)
    }

    private fun stopOnFire(entity: Entity) {
        entity.setDataProperty(EntityDataTypes.TARGET_EID, 0L)
        entity.setDataFlag(EntityFlag.CHARGED, false)
    }
}
