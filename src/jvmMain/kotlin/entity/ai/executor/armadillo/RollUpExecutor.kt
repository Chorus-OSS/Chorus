package org.chorus_oss.chorus.entity.ai.executor.armadillo

import org.chorus_oss.chorus.entity.ai.executor.EntityControl
import org.chorus_oss.chorus.entity.ai.executor.IBehaviorExecutor
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.entity.mob.animal.EntityArmadillo
import org.chorus_oss.chorus.entity.mob.animal.EntityArmadillo.RollState

class RollUpExecutor : EntityControl, IBehaviorExecutor {
    protected var tick: Int = 0

    override fun execute(entity: EntityMob): Boolean {
        if (tick < STAY_TICKS) {
            tick++
            return true
        }
        return false
    }

    override fun onStart(entity: EntityMob) {
        this.tick = 0
        removeLookTarget(entity)
        removeRouteTarget(entity)
        if (entity is EntityArmadillo) {
            entity.setRollState(RollState.ROLLED_UP)
        }
    }

    override fun onStop(entity: EntityMob) {
        if (entity is EntityArmadillo) {
            entity.setRollState(RollState.ROLLED_UP_PEEKING)
        }
    }

    override fun onInterrupt(entity: EntityMob) {
        onStop(entity)
    }

    companion object {
        private const val STAY_TICKS = 60
    }
}
