package org.chorus_oss.chorus.entity.ai.executor

import org.chorus_oss.chorus.entity.ai.memory.NullableMemoryType
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.math.IVector3
import org.chorus_oss.chorus.math.Vector3


open class MoveToTargetExecutor @JvmOverloads constructor(//指示执行器应该从哪个Memory获取目标位置
    protected var memory: NullableMemoryType<out IVector3>,
    protected var speed: Float,
    protected var updateRouteImmediatelyWhenTargetChange: Boolean = false,
    maxFollowRange: Float = -1f,
    minFollowRange: Float = -1f,
    clearDataWhenLose: Boolean = false
) :
    EntityControl, IBehaviorExecutor {
    protected var oldTarget: Vector3? = null
    protected var enableRangeTest: Boolean = false
    protected var maxFollowRangeSquared: Float = 0f
    protected var minFollowRangeSquared: Float = 0f
    protected var clearDataWhenLose: Boolean

    init {
        if (maxFollowRange >= 0 && minFollowRange >= 0) {
            this.maxFollowRangeSquared = maxFollowRange * maxFollowRange
            this.minFollowRangeSquared = minFollowRange * minFollowRange
            enableRangeTest = true
        }
        this.clearDataWhenLose = clearDataWhenLose
    }

    override fun execute(entity: EntityMob): Boolean {
        if (!entity.isEnablePitch) entity.isEnablePitch = true
        if (entity.behaviorGroup.memoryStorage.isEmpty(memory)) {
            return false
        }
        //获取目标位置（这个clone很重要）
        val target = entity.behaviorGroup.memoryStorage[memory]!!.vector3

        //        if (target instanceof Locator locator && !locator.level.getName().equals(entity.level.getName()))
//            return false;
//
//        if(target instanceof Block) {
//            target = target.add(0.5f, 0, 0.5f);
//        }
        if (enableRangeTest) {
            val distanceSquared = target.distanceSquared(entity.position)
            if (distanceSquared > maxFollowRangeSquared || distanceSquared < minFollowRangeSquared) {
                return false
            }
        }

        //更新寻路target
        setRouteTarget(entity, target)
        //更新视线target
        setLookTarget(entity, target)

        if (updateRouteImmediatelyWhenTargetChange) {
            val floor = target.floor()

            if (oldTarget == null || oldTarget == floor) entity.behaviorGroup.isForceUpdateRoute = true

            oldTarget = floor
        }

        if (entity.movementSpeed != speed) entity.movementSpeed = speed

        return true
    }

    override fun onInterrupt(entity: EntityMob) {
        //目标丢失
        removeRouteTarget(entity)
        removeLookTarget(entity)
        //重置速度
        entity.movementSpeed = 0.1f
        entity.isEnablePitch = false
        if (clearDataWhenLose) entity.behaviorGroup.memoryStorage.clear(memory)
    }

    override fun onStop(entity: EntityMob) {
        //目标丢失
        removeRouteTarget(entity)
        removeLookTarget(entity)
        //重置速度
        entity.movementSpeed = 0.1f
        entity.isEnablePitch = false
        if (clearDataWhenLose) entity.behaviorGroup.memoryStorage.clear(memory)
    }
}
