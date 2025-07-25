package org.chorus_oss.chorus.entity.ai.controller

import org.chorus_oss.chorus.entity.data.EntityFlag
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.math.Vector3
import kotlin.math.sqrt

/**
 * 处理飞行/游泳实体运动
 */
class SpaceMoveController : IController {
    override fun control(entity: EntityMob): Boolean {
        if (entity.hasMoveDirection() && !entity.isShouldUpdateMoveDirection) {
            val direction = entity.moveDirectionEnd
            val speed = entity.movementSpeed
            if (entity.motion.x * entity.motion.x + entity.motion.y * entity.motion.y + entity.motion.z * entity.motion.z > speed * speed * 0.4756) {
                return false
            }
            val relativeVector = direction!!.clone().setComponents(
                direction.x - entity.position.x,
                direction.y - entity.position.y, direction.z - entity.position.z
            )
            val xyzLength =
                sqrt(relativeVector.x * relativeVector.x + relativeVector.y * relativeVector.y + relativeVector.z * relativeVector.z)
            val k = speed / xyzLength * 0.33
            val dx = relativeVector.x * k
            val dy = relativeVector.y * k
            val dz = relativeVector.z * k
            entity.addTmpMoveMotion(Vector3(dx, dy, dz))
            entity.setDataFlag(EntityFlag.MOVING, true)
            if (xyzLength < speed) {
                needNewDirection(entity)
                return false
            }
            return true
        } else {
            entity.setDataFlag(EntityFlag.MOVING, false)
            return false
        }
    }

    protected fun needNewDirection(entity: EntityMob) {
        //通知需要新的移动目标
        entity.isShouldUpdateMoveDirection = true
    }
}
