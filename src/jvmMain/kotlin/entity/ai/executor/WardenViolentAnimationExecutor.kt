package org.chorus_oss.chorus.entity.ai.executor

import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.data.EntityFlag
import org.chorus_oss.chorus.entity.mob.EntityMob

class WardenViolentAnimationExecutor(protected var duration: Int) : IBehaviorExecutor {
    protected var currentTick: Int = 0

    override fun execute(entity: EntityMob): Boolean {
        currentTick++
        if (currentTick > duration) return false
        else {
            //更新视线target
            if (entity.memoryStorage.notEmpty(CoreMemoryTypes.ATTACK_TARGET)) entity.lookTarget =
                entity.memoryStorage.get(CoreMemoryTypes.ATTACK_TARGET)?.position
            return true
        }
    }

    override fun onInterrupt(entity: EntityMob) {
        this.currentTick = 0
        entity.setDataFlag(EntityFlag.ROARING, false)
        entity.setDataFlagExtend(EntityFlag.ROARING, false)
    }

    override fun onStart(entity: EntityMob) {
        entity.memoryStorage.set(CoreMemoryTypes.IS_ATTACK_TARGET_CHANGED, false)
        entity.moveTarget = null

        entity.setDataFlag(EntityFlag.ROARING, true)
        entity.setDataFlagExtend(EntityFlag.ROARING, true)
    }

    override fun onStop(entity: EntityMob) {
        this.currentTick = 0
        entity.setDataFlag(EntityFlag.ROARING, false)
        entity.setDataFlagExtend(EntityFlag.ROARING, false)
    }
}
