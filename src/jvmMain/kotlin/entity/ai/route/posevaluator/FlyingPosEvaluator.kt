package org.chorus_oss.chorus.entity.ai.route.posevaluator

import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.math.AxisAlignedBB
import org.chorus_oss.chorus.math.SimpleAxisAlignedBB
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.utils.Utils

open class FlyingPosEvaluator : IPosEvaluator {
    override fun evalPos(entity: EntityMob, pos: Vector3): Boolean {
        //检查是否可到达
        return isPassable(entity, pos)
    }

    /**
     * 指定实体在指定坐标上能否不发生碰撞
     * 对于空间中的移动做了特别的优化
     */
    protected open fun isPassable(entity: EntityMob, vector3: Vector3): Boolean {
        val radius = (entity.getWidth() * entity.getScale()) * 0.5 + 0.1
        val height = entity.getHeight() * entity.getScale()
        // 原版中不会贴地飞行
        val bb: AxisAlignedBB = SimpleAxisAlignedBB(
            vector3.x - radius,
            vector3.y - height * 0.5,
            vector3.z - radius,
            vector3.x + radius,
            vector3.y + height,
            vector3.z + radius
        )
        return !Utils.hasCollisionTickCachedBlocks(entity.getLevel(), bb)
    }
}
