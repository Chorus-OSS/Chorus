package org.chorus_oss.chorus.entity.ai.executor

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.EntityLiving
import org.chorus_oss.chorus.entity.ai.memory.NullableMemoryType
import org.chorus_oss.chorus.entity.data.EntityDataTypes
import org.chorus_oss.chorus.entity.effect.EffectType
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.entity.projectile.EntityProjectile
import org.chorus_oss.chorus.entity.projectile.throwable.EntitySplashPotion
import org.chorus_oss.chorus.event.entity.ProjectileLaunchEvent
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.FloatTag
import org.chorus_oss.chorus.nbt.tag.ListTag
import org.chorus_oss.chorus.plugin.InternalPlugin
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.cos
import kotlin.math.sin

class PotionThrowExecutor(
    protected var memory: NullableMemoryType<out Entity>,
    protected var speed: Float,
    maxShootDistance: Int,
    protected var clearDataWhenLose: Boolean,
    protected val coolDownTick: Int,
    protected val attackDelay: Int
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
        val newTarget = entity.behaviorGroup.memoryStorage[memory]
        if (this.target == null) target = newTarget

        if (!target!!.isAlive()) return false
        else if (target is Player) {
            val player = target as Player
            if (player.isCreative || player.isSpectator || !player.isOnline || (entity.level!!.name != player.level!!.name)) {
                return false
            }
        }

        if (target!!.locator != newTarget!!.locator) {
            target = newTarget
        }

        if (entity.movementSpeed != speed) entity.movementSpeed = speed
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
                startShootSequence(entity)
            }
        } else if (tick2 != 0) {
            tick2++
            if (tick2 > attackDelay) {
                throwPotion(entity)
                entity.level!!.scheduler.scheduleDelayedTask(
                    InternalPlugin.INSTANCE,
                    { endShootSequence(entity) }, 20
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
        entity.movementSpeed = EntityLiving.Companion.DEFAULT_SPEED
        if (clearDataWhenLose) {
            entity.behaviorGroup.memoryStorage.clear(memory)
        }
        entity.isEnablePitch = false
        endShootSequence(entity)
        this.target = null
    }

    override fun onInterrupt(entity: EntityMob) {
        removeRouteTarget(entity)
        removeLookTarget(entity)
        entity.movementSpeed = EntityLiving.Companion.DEFAULT_SPEED
        if (clearDataWhenLose) {
            entity.behaviorGroup.memoryStorage.clear(memory)
        }
        entity.isEnablePitch = false
        endShootSequence(entity)
        this.target = null
    }

    protected fun throwPotion(entity: EntityMob) {
        val potionTransform = entity.transform
        val directionVector = entity.getDirectionVector()
        potionTransform.setY(entity.position.y + entity.getEyeHeight() + directionVector.y)
        val nbt = CompoundTag()
            .putList(
                "Pos", ListTag<FloatTag>()
                    .add(FloatTag(potionTransform.position.x))
                    .add(FloatTag(potionTransform.position.y))
                    .add(FloatTag(potionTransform.position.z))
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
            .putInt("PotionId", getPotionEffect(entity))

        val projectile: Entity = Entity.Companion.createEntity(
            EntityID.SPLASH_POTION,
            entity.level!!.getChunk(entity.position.chunkX, entity.position.chunkZ),
            nbt
        )
            ?: return

        if (projectile is EntitySplashPotion) {
            projectile.shootingEntity = entity
        }

        val projectev = ProjectileLaunchEvent(projectile as EntityProjectile, entity)
        Server.instance.pluginManager.callEvent(projectev)
        if (projectev.cancelled) {
            projectile.kill()
        } else {
            projectile.spawnToAll()
        }
    }

    private fun startShootSequence(entity: Entity) {
        entity.setDataProperty(EntityDataTypes.TARGET_EID, target!!.getRuntimeID())
    }

    private fun endShootSequence(entity: Entity) {
        entity.setDataProperty(EntityDataTypes.TARGET_EID, 0L)
    }

    fun getPotionEffect(entity: Entity): Int {
        if (entity is EntityMob) {
            if (entity.memoryStorage.notEmpty(memory)) {
                val target = entity.memoryStorage[memory]!!
                val distance = target.position.distance(entity.position)
                if (distance > 8 && !target.hasEffect(EffectType.SLOWNESS)) {
                    return 17 //SLOWNESS
                } else if (distance < 3 && !target.hasEffect(EffectType.WEAKNESS) && ThreadLocalRandom.current()
                        .nextInt(4) == 0
                ) {
                    return 34 //WEAKNESS
                } else if (target.health > 8 && !target.hasEffect(EffectType.POISON)) {
                    return 25 //POISON
                }
            }
        }
        return 23 //INSTANT_DAMAGE
    }
}
