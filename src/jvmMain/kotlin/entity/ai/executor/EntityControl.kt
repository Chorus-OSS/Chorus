package org.chorus_oss.chorus.entity.ai.executor

import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.math.Vector3

/**
 * 封装了一些涉及控制器的方法.
 *
 *
 * Involving some methods about controller.
 */
interface EntityControl {
    fun setRouteTarget(entity: EntityMob, vector3: Vector3?) {
        entity.moveTarget = vector3
    }

    fun setLookTarget(entity: EntityMob, vector3: Vector3?) {
        entity.lookTarget = vector3
    }

    fun removeRouteTarget(entity: EntityMob) {
        entity.moveTarget = null
    }

    fun removeLookTarget(entity: EntityMob) {
        entity.lookTarget = null
    }
}
