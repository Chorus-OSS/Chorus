package org.chorus_oss.chorus.entity.ai.controller

import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.math.BVector3
import org.chorus_oss.chorus.math.Vector3

/**
 * 处理实体Pitch/Yaw/HeadYaw
 */
class LookController(protected var lookAtTarget: Boolean, protected var lookAtRoute: Boolean) : IController {
    override fun control(entity: EntityMob): Boolean {
        val lookTarget = entity.lookTarget

        if (lookAtRoute && entity.hasMoveDirection()) {
            //clone防止异步导致的NPE
            val moveDirectionEnd = entity.moveDirectionEnd!!.clone()
            //构建路径方向向量
            val routeDirectionVector = Vector3(
                moveDirectionEnd.x - entity.position.x,
                moveDirectionEnd.y - entity.position.y,
                moveDirectionEnd.z - entity.position.z
            )
            val yaw = BVector3.getYawFromVector(routeDirectionVector)
            entity.rotation.yaw = (yaw)
            if (!lookAtTarget) {
                entity.headYaw = (yaw)
                if (entity.isEnablePitch) entity.rotation.pitch = (BVector3.getPitchFromVector(routeDirectionVector))
            }
        }
        if (lookAtTarget && lookTarget != null) {
            //构建指向玩家的向量
            val toPlayerVector = Vector3(
                lookTarget.x - entity.position.x,
                lookTarget.y - entity.position.y,
                lookTarget.z - entity.position.z
            )
            if (entity.isEnablePitch) entity.rotation.pitch = (BVector3.getPitchFromVector(toPlayerVector))
            entity.headYaw = (BVector3.getYawFromVector(toPlayerVector))
        }
        if (!entity.isEnablePitch) entity.rotation.pitch = (0).toDouble()
        return true
    }
}
