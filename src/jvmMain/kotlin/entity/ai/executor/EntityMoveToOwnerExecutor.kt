package org.chorus_oss.chorus.entity.ai.executor

import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityOwnable
import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.data.EntityFlag
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.math.Vector3
import java.util.concurrent.ThreadLocalRandom

/**
 * 实体移动到主人身边.(只对实现了接口 [EntityOwnable][org.chorus_oss.chorus.entity.EntityOwnable] 的实体有效)
 *
 *
 * The entity moves to the master's side.(Only valid for entities that implement the interface [EntityOwnable][org.chorus_oss.chorus.entity.EntityOwnable])
 */
class EntityMoveToOwnerExecutor @JvmOverloads constructor(
    protected var speed: Float,
    protected var updateRouteImmediatelyWhenTargetChange: Boolean,
    maxFollowRange: Int,
    minFollowRange: Int = 9
) :
    EntityControl, IBehaviorExecutor {
    protected var maxFollowRangeSquared: Int = 0
    var minFollowRangeSquared: Int = 0
    protected var oldTarget: Vector3? = null

    init {
        if (maxFollowRange >= 0) {
            this.maxFollowRangeSquared = maxFollowRange * maxFollowRange
        }
        if (minFollowRange >= 0) {
            this.minFollowRangeSquared = minFollowRange * minFollowRange
        }
    }

    override fun execute(entity: EntityMob): Boolean {
        if (!entity.isEnablePitch) entity.isEnablePitch = true

        if (entity is EntityOwnable) {
            val player = entity.owner ?: return false

            //获取目的地位置（这个clone很重要）
            val target = player
            if (target.position.distanceSquared(entity.position) <= minFollowRangeSquared) return false

            //不允许跨世界
            if (target.level!!.name != entity.level!!.name) return false

            if (entity.locator.position.floor() == oldTarget) return false

            val distanceSquared = entity.position.distanceSquared(player.position)
            if (distanceSquared <= maxFollowRangeSquared) {
                //更新寻路target
                setRouteTarget(entity, target.position)
                //更新视线target
                setLookTarget(entity, target.position)

                if (entity.memoryStorage.notEmpty(CoreMemoryTypes.NEAREST_FEEDING_PLAYER)) {
                    entity.setDataFlag(EntityFlag.INTERESTED, true)
                }

                if (updateRouteImmediatelyWhenTargetChange) {
                    val floor = target.position.floor()

                    if (oldTarget == null || oldTarget == floor) entity.behaviorGroup.isForceUpdateRoute = true

                    oldTarget = floor
                }

                if (entity.movementSpeed != speed) entity.movementSpeed = speed

                return true
            } else {
                val targetVector = randomVector3(player, 4)
                return if (targetVector == null || targetVector.distanceSquared(player.position) > maxFollowRangeSquared) true //继续寻找
                else !entity.teleport(targetVector)
            }
        }
        return false
    }

    override fun onInterrupt(entity: EntityMob) {
        //目标丢失
        removeRouteTarget(entity)
        removeLookTarget(entity)
        //重置速度
        entity.movementSpeed = 1.2f
        entity.isEnablePitch = false
        if (entity.memoryStorage.isEmpty(CoreMemoryTypes.NEAREST_FEEDING_PLAYER)) {
            entity.setDataFlag(EntityFlag.INTERESTED, false)
        }
        oldTarget = null
    }

    override fun onStop(entity: EntityMob) {
        //目标丢失
        removeRouteTarget(entity)
        removeLookTarget(entity)
        //重置速度
        entity.movementSpeed = 1.2f
        entity.isEnablePitch = false
        if (entity.memoryStorage.isEmpty(CoreMemoryTypes.NEAREST_FEEDING_PLAYER)) {
            entity.setDataFlag(EntityFlag.INTERESTED, false)
        }
        oldTarget = null
    }

    protected fun randomVector3(player: Entity, r: Int): Vector3? {
        val random = ThreadLocalRandom.current()
        val x = random.nextInt(r * -1, r) + player.position.floorX
        val z = random.nextInt(r * -1, r) + player.position.floorZ
        val y = player.level!!.getHighestBlockAt(x, z).toDouble()
        val vector3 = Vector3(x.toDouble(), y, z.toDouble())
        val result = player.level!!.getBlock(vector3)
        return if (result.isSolid && result.id !== BlockID.AIR) result.up().position
        else null
    }
}
