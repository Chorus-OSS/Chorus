package org.chorus_oss.chorus.entity.ai.executor

import org.chorus_oss.chorus.entity.EntityLiving
import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.ai.memory.NullableMemoryType
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.math.IVector3
import org.chorus_oss.chorus.math.Vector3


open class FleeFromTargetExecutor @JvmOverloads constructor(//指示执行器应该从哪个Memory获取目标位置
    protected var memory: NullableMemoryType<out IVector3>,
    protected var speed: Float,
    protected var updateRouteImmediatelyWhenTargetChange: Boolean = false,
    protected var minDistance: Float = -1f,
    protected var clearDataWhenLose: Boolean = false
) :
    EntityControl, IBehaviorExecutor {
    protected var oldTarget: Vector3? = null
    protected var enableRangeTest: Boolean = false

    override fun execute(entity: EntityMob): Boolean {
        if (!entity.isEnablePitch) entity.isEnablePitch = true
        if (entity.behaviorGroup.memoryStorage.isEmpty(memory)) {
            return false
        }

        val target = entity.behaviorGroup.memoryStorage[memory]!!.vector3
        val moveTarget = target.add(
            Vector3(
                entity.position.x - target.x,
                entity.position.y - target.y,
                entity.position.z - target.z
            ).normalize().multiply(minDistance.toDouble())
        )

        if (target.distance(entity.position) > minDistance) {
            setLookTarget(entity, target)
            return false
        }

        setRouteTarget(entity, moveTarget)
        setLookTarget(entity, moveTarget)

        // This gives the Evoker enough time to turn around before attacking.
        entity.memoryStorage.set(CoreMemoryTypes.LAST_ATTACK_TIME, entity.level!!.tick)

        if (updateRouteImmediatelyWhenTargetChange) {
            val floor = target.floor()

            if (oldTarget == null || oldTarget == floor) entity.behaviorGroup.isForceUpdateRoute = true

            oldTarget = floor
        }

        if (entity.movementSpeed != speed) entity.movementSpeed = speed

        return true
    }

    override fun onInterrupt(entity: EntityMob) {
        removeRouteTarget(entity)
        removeLookTarget(entity)
        entity.movementSpeed = EntityLiving.Companion.DEFAULT_SPEED
        entity.isEnablePitch = false
        if (clearDataWhenLose) entity.behaviorGroup.memoryStorage.clear(memory)
    }

    override fun onStop(entity: EntityMob) {
        removeRouteTarget(entity)
        removeLookTarget(entity)
        entity.movementSpeed = EntityLiving.Companion.DEFAULT_SPEED
        entity.isEnablePitch = false
        if (clearDataWhenLose) entity.behaviorGroup.memoryStorage.clear(memory)
    }
}
