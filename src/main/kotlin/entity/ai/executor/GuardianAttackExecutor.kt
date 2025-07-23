package org.chorus_oss.chorus.entity.ai.executor

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityLiving
import org.chorus_oss.chorus.entity.ai.memory.NullableMemoryType
import org.chorus_oss.chorus.entity.data.EntityDataTypes
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.entity.mob.monster.EntityMonster
import org.chorus_oss.chorus.event.entity.EntityDamageByEntityEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent.DamageCause
import org.chorus_oss.protocol.packets.ActorEventPacket

class GuardianAttackExecutor(
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

        setLookTarget(entity, clone.position)

        if (tick2 == 0 && tick1 > coolDownTick) {
            if (entity.position.distanceSquared(target!!.position) <= maxShootDistanceSquared) {
                this.tick1 = 0
                tick2++
                startSequence(entity)
            }
        } else if (tick2 != 0) {
            tick2++
            if (tick2 > attackDelay) {
                if (entity.getDataProperty<Long>(EntityDataTypes.TARGET_EID, 0L) == target!!.getRuntimeID()) {
                    val event = EntityDamageByEntityEvent(
                        entity,
                        target!!,
                        DamageCause.ENTITY_ATTACK,
                        (entity as EntityMonster).getDiffHandDamage(Server.instance.getDifficulty())
                    )
                    target!!.attack(event)
                    if (Server.instance.getDifficulty() >= 2) {
                        val event2 = EntityDamageByEntityEvent(
                            entity,
                            target!!, DamageCause.MAGIC, 1f
                        )
                        target!!.attack(event2)
                    }
                }
                endSequence(entity)
                tick2 = 0
                return target!!.health != 0f
            }
        }
        return true
    }

    override fun onStop(entity: EntityMob) {
        removeLookTarget(entity)
        entity.movementSpeed = EntityLiving.Companion.DEFAULT_SPEED
        if (clearDataWhenLose) {
            entity.behaviorGroup.memoryStorage.clear(memory)
        }
        entity.isEnablePitch = false
        endSequence(entity)
        this.target = null
    }

    override fun onInterrupt(entity: EntityMob) {
        removeLookTarget(entity)
        entity.movementSpeed = EntityLiving.Companion.DEFAULT_SPEED
        if (clearDataWhenLose) {
            entity.behaviorGroup.memoryStorage.clear(memory)
        }
        entity.isEnablePitch = false
        endSequence(entity)
        this.target = null
    }

    private fun startSequence(entity: Entity) {
        entity.setDataProperty(EntityDataTypes.TARGET_EID, target!!.getRuntimeID())
        entity.level!!.addLevelSoundEvent(
            entity.position,
            org.chorus_oss.protocol.packets.LevelSoundEventPacket.Companion.SoundType.MobWarning,
            -1,
            entity.getEntityIdentifier(),
            false,
            false
        )
    }

    private fun endSequence(entity: Entity) {
        entity.setDataProperty(EntityDataTypes.TARGET_EID, 0L)
        val pk = ActorEventPacket(
            actorRuntimeID = entity.getRuntimeID().toULong(),
            eventType = ActorEventPacket.Companion.Type.GuardianAttackAnimation,
            eventData = 0
        )
        Server.broadcastPacket(entity.viewers.values, pk)
    }
}
