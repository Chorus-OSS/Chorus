package org.chorus_oss.chorus.entity.ai.executor

import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.data.EntityFlag
import org.chorus_oss.chorus.entity.mob.EntityMob

/**
 * 实体看向最近携带食物的玩家<br></br>
 * 此执行器与 [LookAtTargetExecutor] 最大的一点不同是，它会设置实体的DATA_FLAG_INTERESTED为true
 *
 *
 * Entity looks at the nearest player carrying the food<br></br>
 * The biggest difference between this executor and [LookAtTargetExecutor] is that it will set the entity's DATA_FLAG_INTERESTED to true
 */
class LookAtFeedingPlayerExecutor : EntityControl, IBehaviorExecutor {
    override fun execute(entity: EntityMob): Boolean {
        if (!entity.isEnablePitch) entity.isEnablePitch = true
        val vector3 = entity.memoryStorage.get(CoreMemoryTypes.NEAREST_FEEDING_PLAYER)
        if (vector3 != null) {
            setLookTarget(entity, vector3.position)
            entity.setDataFlag(EntityFlag.INTERESTED, true)
            return true
        } else return false
    }

    override fun onInterrupt(entity: EntityMob) {
        entity.isEnablePitch = false
        if (entity.memoryStorage.isEmpty(CoreMemoryTypes.NEAREST_FEEDING_PLAYER)) {
            entity.setDataFlag(EntityFlag.INTERESTED, false)
        }
        removeLookTarget(entity)
    }

    override fun onStop(entity: EntityMob) {
        entity.isEnablePitch = false
        if (entity.memoryStorage.isEmpty(CoreMemoryTypes.NEAREST_FEEDING_PLAYER)) {
            entity.setDataFlag(EntityFlag.INTERESTED, false)
        }
        removeLookTarget(entity)
    }
}
