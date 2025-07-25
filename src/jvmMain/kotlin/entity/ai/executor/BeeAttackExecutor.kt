package org.chorus_oss.chorus.entity.ai.executor

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.ai.memory.NullableMemoryType
import org.chorus_oss.chorus.entity.effect.Effect
import org.chorus_oss.chorus.entity.effect.EffectType
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.entity.mob.animal.EntityBee

class BeeAttackExecutor(
    memory: NullableMemoryType<out Entity>,
    speed: Float,
    maxSenseRange: Int,
    clearDataWhenLose: Boolean,
    coolDown: Int
) :
    MeleeAttackExecutor(memory, speed, maxSenseRange, clearDataWhenLose, coolDown) {

    override fun execute(entity: EntityMob): Boolean {
        if (entity is EntityBee) {
            if (entity.memoryStorage.notEmpty(CoreMemoryTypes.ATTACK_TARGET)) {
                if (!entity.isEnablePitch) entity.isEnablePitch = true
                val entity1 = entity.memoryStorage[CoreMemoryTypes.ATTACK_TARGET]
                if (entity1 != null) {
                    this.lookTarget = entity1.position.clone()
                    if (Server.instance.getDifficulty() == 2) {
                        entity1.addEffect(Effect.get(EffectType.POISON).setDuration(200))
                    } else if (Server.instance.getDifficulty() == 3) {
                        entity1.addEffect(Effect.get(EffectType.POISON).setDuration(360))
                    }
                }
            }

            if (entity.position.distanceSquared(
                    entity.memoryStorage[CoreMemoryTypes.ATTACK_TARGET]!!.position
                ) <= 2.5 && attackTick > coolDown && entity.hasSting()
            ) {
                entity.dieInTicks = 700
            }
            return super.execute(entity)
        }
        return false
    }

    override fun onStop(entity: EntityMob) {
        stop(entity)
        super.onStop(entity)
    }

    override fun onInterrupt(entity: EntityMob) {
        stop(entity)
        super.onInterrupt(entity)
    }

    private fun stop(entity: EntityMob) {
        val bee = entity as EntityBee
        entity.level!!.scheduler.scheduleDelayedTask(null, { bee.setAngry(false) }, 5)
    }
}
