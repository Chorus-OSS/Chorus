package org.chorus_oss.chorus.entity.ai.controller

import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.mob.EntityMob

/**
 * 下潜运动控制器，使实体下潜
 */
class DiveController : IController {
    override fun control(entity: EntityMob): Boolean {
        // add dive force
        if (entity.memoryStorage.get(CoreMemoryTypes.ENABLE_DIVE_FORCE))  // 抵消额外的浮力即可
            entity.motion.y -= entity.getGravity() * (entity.getFloatingForceFactor() - 1)
        return true
    }
}
