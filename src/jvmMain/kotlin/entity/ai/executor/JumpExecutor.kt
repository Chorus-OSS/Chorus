package org.chorus_oss.chorus.entity.ai.executor

import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.math.Vector3


class JumpExecutor : IBehaviorExecutor {
    override fun execute(entity: EntityMob): Boolean {
        entity.setMotion(Vector3(0.0, entity.getJumpingMotion(0.4), 0.0))
        return true
    }
}
