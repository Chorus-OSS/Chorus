package org.chorus_oss.chorus.entity.ai.route.posevaluator

import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.math.AxisAlignedBB
import org.chorus_oss.chorus.math.SimpleAxisAlignedBB
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.utils.Utils

/**
 * 用于游泳实体的坐标评估器
 */
class SwimmingPosEvaluator : IPosEvaluator {
    override fun evalPos(entity: EntityMob, pos: Vector3): Boolean {
        val blockId = entity.level!!.getTickCachedBlock(pos).id
        return isPassable(entity, pos) && (blockId === BlockID.FLOWING_WATER || blockId === BlockID.WATER)
    }

    override fun evalStandingBlock(entity: EntityMob, block: Block): Boolean {
        return true
    }

    /**
     * 指定实体在指定坐标上能否不发生碰撞
     * 对于空间中的移动做了特别的优化
     */
    protected fun isPassable(entity: EntityMob, vector3: Vector3): Boolean {
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
