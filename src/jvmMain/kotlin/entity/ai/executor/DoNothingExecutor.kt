package org.chorus_oss.chorus.entity.ai.executor

import org.chorus_oss.chorus.entity.mob.EntityMob

class DoNothingExecutor : IBehaviorExecutor {
    override fun execute(entity: EntityMob): Boolean {
        return true
    }
}
