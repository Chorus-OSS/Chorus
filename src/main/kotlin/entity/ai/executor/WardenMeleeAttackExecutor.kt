package org.chorus_oss.chorus.entity.ai.executor

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.ai.memory.NullableMemoryType
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.entity.mob.monster.EntityWarden
import org.chorus_oss.chorus.event.entity.EntityDamageByEntityEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent.DamageCause
import org.chorus_oss.chorus.event.entity.EntityDamageEvent.DamageModifier
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.protocol.packets.ActorEventPacket
import java.util.*

class WardenMeleeAttackExecutor(
    protected var memory: NullableMemoryType<out Entity>,
    protected var damage: Float,
    protected var speed: Float
) :
    EntityControl, IBehaviorExecutor {
    protected var attackTick: Int = 0
    protected var coolDown: Int = 0
    protected var oldTarget: Vector3? = null

    override fun execute(entity: EntityMob): Boolean {
        attackTick++
        if (entity.behaviorGroup.memoryStorage.isEmpty(memory)) return false
        if (entity.movementSpeed != speed) entity.movementSpeed = speed
        //获取目标位置（这个clone很重要）
        val target = entity.behaviorGroup.memoryStorage[memory]
        if (!target!!.isAlive()) return false
        this.coolDown = calCoolDown(entity, target)
        val clonedTarget = target.position.clone()
        //更新寻路target
        setRouteTarget(entity, clonedTarget)
        //更新视线target
        setLookTarget(entity, clonedTarget)

        val floor = clonedTarget.floor()

        if (oldTarget == null || oldTarget != floor) entity.behaviorGroup.isForceUpdateRoute = true

        oldTarget = floor

        if (entity.position.distanceSquared(target.position) <= 4 && attackTick > coolDown) {
            val damages: MutableMap<DamageModifier, Float> = EnumMap(
                DamageModifier::class.java
            )
            damages[DamageModifier.BASE] = damage

            val ev = EntityDamageByEntityEvent(
                entity,
                target, DamageCause.ENTITY_ATTACK, damages, 0.6f, null
            )

            ev.isBreakShield = true
            target.attack(ev)
            playAttackAnimation(entity)
            entity.level!!.addSound(target.position, Sound.MOB_WARDEN_ATTACK)
            attackTick = 0
            return target.isUndead()
        }
        return true
    }

    protected fun calCoolDown(entity: EntityMob, target: Entity?): Int {
        if (entity is EntityWarden) {
            val anger = entity.memoryStorage[CoreMemoryTypes.WARDEN_ANGER_VALUE].getOrDefault(target, 0)
            return if (anger >= 145) 18 else 36
        } else {
            return 20
        }
    }

    override fun onStart(entity: EntityMob) {
        if (!entity.isEnablePitch) entity.isEnablePitch = true
    }

    override fun onStop(entity: EntityMob) {
        removeRouteTarget(entity)
        removeLookTarget(entity)
        //重置速度
        entity.movementSpeed = 0.1f
        entity.isEnablePitch = false
    }

    override fun onInterrupt(entity: EntityMob) {
        removeRouteTarget(entity)
        removeLookTarget(entity)
        //重置速度
        entity.movementSpeed = 0.1f
        entity.isEnablePitch = false
    }

    protected fun playAttackAnimation(entity: EntityMob) {
        val pk = ActorEventPacket(
            actorRuntimeID = entity.getRuntimeID().toULong(),
            eventType = ActorEventPacket.Companion.Type.ArmSwing,
            eventData = 0
        )
        Server.broadcastPacket(entity.viewers.values, pk)
    }
}
