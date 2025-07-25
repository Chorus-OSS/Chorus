package org.chorus_oss.chorus.entity.ai.route.posevaluator

import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.BlockFence
import org.chorus_oss.chorus.block.BlockFenceGate
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.math.AxisAlignedBB
import org.chorus_oss.chorus.math.SimpleAxisAlignedBB
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.utils.Utils

/**
 * 用于标准陆地行走实体的方块评估器
 */
class WalkingPosEvaluator : IPosEvaluator {
    override fun evalStandingBlock(entity: EntityMob, block: Block): Boolean {
        //居中坐标
        val blockCenter = block.add(0.5, 1.0, 0.5)
        //检查是否可到达
        if (!isPassable(entity, blockCenter.position)) return false
        if (entity.hasWaterAt(0f) && blockCenter.y - entity.position.y > 1)  //实体在水中不能移动到一格高以上的方块
            return false
        //TODO: 检查碰头
        //脚下不能是伤害性方块
        if (block.id === BlockID.FLOWING_LAVA || block.id === BlockID.LAVA || block.id === BlockID.CACTUS) return false
        //不能是栏杆
        if (block is BlockFence || block is BlockFenceGate) return false
        //水特判
        if (block.id === BlockID.WATER || block.id === BlockID.FLOWING_WATER) return true
        //必须可以站立
        return !block.canPassThrough()
    }

    /**
     * 指定实体在指定坐标上能否不发生碰撞
     */
    // TODO: 此方法会造成大量开销，原因是碰撞检查，有待优化
    protected fun isPassable(entity: EntityMob, vector3: Vector3): Boolean {
        val radius = ((entity.getWidth() * entity.getScale()) / 2).toDouble()
        val height = entity.getHeight() * entity.getScale()
        val bb: AxisAlignedBB = SimpleAxisAlignedBB(
            vector3.x - radius,
            vector3.y,
            vector3.z - radius,
            vector3.x + radius,
            vector3.y + height,
            vector3.z + radius
        )
        if (radius > 0.5) {
            // A --- B --- C
            // |           |
            // D     P     E
            // |           |
            // F --- G --- H
            // 在P点一次通过的可能性最大，所以优先检测
            val collisionInfo = Utils.hasCollisionTickCachedBlocksWithInfo(entity.getLevel(), bb)
            if (collisionInfo.toInt() == 0) {
                return true
            }
            // 将实体碰撞箱分别对齐A B C D E F G H处，检测是否能通过
            val dr = radius - 0.5
            for (i in -1..1) {
                // collisionInfo & 0b110000：获取x轴的碰撞信息，3为在大于中心的方向膨胀，1为小于中心的方向碰撞，0为没有碰撞
                // -2：是为了将3转换为1，1转换为-1，0转换为-2
                // 然后进行判断，如果i的值跟上面的值相等，说明此方向已经100%会碰撞了，不需要再检测
                if (((collisionInfo.toInt() and 48) shr 4) - 2 == i) continue
                for (j in -1..1) {
                    if (i == 0 && j == 0) continue  // P点已经被检测过了

                    if ((collisionInfo.toInt() and 3) - 2 == j) continue  // 获取z轴的碰撞信息并比较

                    // 由于已经缓存了方块，检测速度还是可以接受的
                    if (!Utils.hasCollisionTickCachedBlocks(
                            entity.getLevel(),
                            bb.clone().offset(i * dr, 0.0, j * dr)
                        )
                    ) {
                        return true
                    }
                }
            }
            return false
        } else {
            return !Utils.hasCollisionTickCachedBlocks(entity.getLevel(), bb)
        }
    }
}
