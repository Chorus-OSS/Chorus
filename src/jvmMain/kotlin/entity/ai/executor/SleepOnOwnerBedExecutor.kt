package org.chorus_oss.chorus.entity.ai.executor

import org.chorus_oss.chorus.entity.EntityOwnable
import org.chorus_oss.chorus.entity.data.EntityFlag
import org.chorus_oss.chorus.entity.mob.EntityMob

/**
 * 使有主人的生物在主人睡觉时睡到主人床上<br></br>
 * 只能在实现了接口 [EntityOwnable] 的实体上使用<br></br>
 * 需要保证实体的getOwner()方法返回非空
 */
class SleepOnOwnerBedExecutor : IBehaviorExecutor {
    override fun execute(entity: EntityMob): Boolean {
        val owner = (entity as EntityOwnable).owner
        if (entity.position.distanceSquared(owner!!.position) <= 4) {
            setSleeping(entity, true)
        }
        return owner.isSleeping()
    }

    override fun onStart(entity: EntityMob) {
        val owner = (entity as EntityOwnable).owner
        entity.moveTarget = owner!!.position
        entity.lookTarget = owner.position
    }

    override fun onInterrupt(entity: EntityMob) {
        stop(entity)
    }

    override fun onStop(entity: EntityMob) {
        stop(entity)
    }

    protected fun stop(entity: EntityMob) {
        setSleeping(entity, false)
    }

    protected fun setSleeping(entity: EntityMob, sleeping: Boolean) {
        entity.setDataFlag(EntityFlag.RESTING, sleeping)
        entity.setDataFlagExtend(EntityFlag.RESTING, sleeping)
    }
}
