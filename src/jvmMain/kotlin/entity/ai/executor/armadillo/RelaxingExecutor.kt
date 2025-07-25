package org.chorus_oss.chorus.entity.ai.executor.armadillo

import org.chorus_oss.chorus.entity.ai.executor.EntityControl
import org.chorus_oss.chorus.entity.ai.executor.IBehaviorExecutor
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.entity.mob.animal.EntityArmadillo
import org.chorus_oss.chorus.entity.mob.animal.EntityArmadillo.RollState

class RelaxingExecutor : EntityControl, IBehaviorExecutor {
    override fun execute(entity: EntityMob): Boolean {
        return false
    }

    override fun onStart(entity: EntityMob) {
        removeLookTarget(entity)
        removeRouteTarget(entity)
        if (entity is EntityArmadillo) {
            entity.setRollState(RollState.ROLLED_UP_RELAXING)
        }
    }
}
