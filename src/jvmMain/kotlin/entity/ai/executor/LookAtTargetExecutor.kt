package org.chorus_oss.chorus.entity.ai.executor

import org.chorus_oss.chorus.entity.ai.memory.NullableMemoryType
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.math.IVector3

class LookAtTargetExecutor(//指示执行器应该从哪个Memory获取目标位置
    protected var memory: NullableMemoryType<out IVector3>, protected var duration: Int
) :
    EntityControl, IBehaviorExecutor {
    protected var currentTick: Int = 0

    override fun execute(entity: EntityMob): Boolean {
        currentTick++
        if (!entity.isEnablePitch) entity.isEnablePitch = true
        val vector3Memory = entity.memoryStorage[memory]
        if (vector3Memory != null) {
            setLookTarget(entity, vector3Memory.vector3)
        }
        return currentTick <= duration
    }

    override fun onInterrupt(entity: EntityMob) {
        currentTick = 0
        entity.isEnablePitch = false
    }

    override fun onStop(entity: EntityMob) {
        currentTick = 0
        entity.isEnablePitch = false
    }
}
