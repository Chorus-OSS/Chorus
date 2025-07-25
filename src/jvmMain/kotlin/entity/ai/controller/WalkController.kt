package org.chorus_oss.chorus.entity.ai.controller

import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.BlockCarpet
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.block.BlockLiquid
import org.chorus_oss.chorus.entity.EntityPhysical
import org.chorus_oss.chorus.entity.data.EntityFlag
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.math.Vector3
import java.util.*
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * 处理陆地行走实体运动
 * todo: 有待解耦
 */
open class WalkController : IController {
    protected open var currentJumpCoolDown: Int = 0

    private fun canJump(block: Block): Boolean {
        return if (block.isSolid) true
        else if (block is BlockCarpet) true
        else block.id === BlockID.FLOWER_POT || block.id === BlockID.CAKE
    }

    override fun control(entity: EntityMob): Boolean {
        currentJumpCoolDown++

        if (currentJumpCoolDown > JUMP_COOL_DOWN && !entity.isOnGround() && !entity.isTouchingWater()) {
            return false
        }

        if (entity.hasMoveDirection() && !entity.isShouldUpdateMoveDirection) {
            //clone防止异步导致的NPE
            val direction = entity.moveDirectionEnd!!.clone()
            val speed = entity.movementSpeed
            if (entity.motion.x * entity.motion.x + entity.motion.z * entity.motion.z > speed * speed * 0.4756) {
                entity.setDataFlag(EntityFlag.MOVING, false)
                return false
            }
            val relativeVector = direction.clone().setComponents(
                direction.x - entity.position.x,
                direction.y - entity.position.y, direction.z - entity.position.z
            )
            val xzLengthSquared = relativeVector.x * relativeVector.x + relativeVector.z * relativeVector.z
            if (abs(xzLengthSquared) < EntityPhysical.Companion.PRECISION) {
                entity.setDataFlag(EntityFlag.MOVING, false)
                return false
            }
            val xzLength = sqrt(relativeVector.x * relativeVector.x + relativeVector.z * relativeVector.z)
            val k = speed / xzLength * 0.33
            val dx = relativeVector.x * k
            val dz = relativeVector.z * k
            var dy = 0.0
            val target = entity.level!!.getBlock(entity.moveDirectionStart!!)
            if (target.down().isSolid && relativeVector.y > 0 && collidesBlocks(
                    entity,
                    dx,
                    0.0,
                    dz
                ) && currentJumpCoolDown > JUMP_COOL_DOWN || (entity.isTouchingWater() && !(target is BlockLiquid || target.level
                    .getBlock(target.position, 1) is BlockLiquid) && target.down().isSolid)
            ) {
                //note: 从对BDS的抓包信息来看，台阶的碰撞箱在服务端和半砖一样，高度都为0.5
                val collisionBlocks = entity.level!!.getTickCachedCollisionBlocks(
                    entity.offsetBoundingBox.getOffsetBoundingBox(dx, dy, dz),
                    targetFirst = false,
                    ignoreCollidesCheck = false
                ) { block: Block -> this.canJump(block) }
                //计算出需要向上移动的高度
                val maxY = Arrays.stream(collisionBlocks).map { b: Block -> b.collisionBoundingBox!!.maxY }
                    .max { obj: Double, anotherDouble: Double? ->
                        obj.compareTo(
                            anotherDouble!!
                        )
                    }.orElse(0.0)
                val diffY = maxY - entity.position.y
                dy += entity.getJumpingMotion(diffY)
                currentJumpCoolDown = 0
            }
            entity.addTmpMoveMotion(Vector3(dx, dy, dz))
            entity.setDataFlag(EntityFlag.MOVING, true)
            if (xzLength < speed) {
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

    protected fun collidesBlocks(entity: EntityMob, dx: Double, dy: Double, dz: Double): Boolean {
        return entity.level!!.getTickCachedCollisionBlocks(
            entity.offsetBoundingBox.getOffsetBoundingBox(dx, dy, dz), targetFirst = true,
            ignoreCollidesCheck = false
        ) { block: Block -> this.canJump(block) }.isNotEmpty()
    }

    companion object {
        protected const val JUMP_COOL_DOWN: Int = 10
    }
}
