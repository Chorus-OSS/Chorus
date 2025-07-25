package org.chorus_oss.chorus.entity.ai.executor

import org.chorus_oss.chorus.entity.EntityLiving
import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.ai.memory.NullableMemoryType
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.level.Transform
import org.chorus_oss.chorus.math.IVector3
import org.chorus_oss.chorus.math.Vector3
import kotlin.math.cos
import kotlin.math.sin


class CircleAboveTargetExecutor @JvmOverloads constructor(//指示执行器应该从哪个Memory获取目标位置
    protected var memory: NullableMemoryType<out IVector3>,
    protected var speed: Float,
    protected var updateRouteImmediatelyWhenTargetChange: Boolean = false,
    protected var clearDataWhenLose: Boolean = false
) :
    EntityControl, IBehaviorExecutor {
    protected var oldTarget: Vector3? = null
    protected var enableRangeTest: Boolean = false

    var sections: Int = 8

    private var circleLoc = 0


    override fun execute(entity: EntityMob): Boolean {
        if (!entity.isEnablePitch) entity.isEnablePitch = true
        if (entity.behaviorGroup.memoryStorage.isEmpty(memory)) {
            return false
        }
        val target = entity.behaviorGroup.memoryStorage[memory]!!.vector3
        val origin =
            entity.behaviorGroup.memoryStorage[CoreMemoryTypes.LAST_ATTACK_ENTITY]!!.transform.add(
                0.0,
                24.0,
                0.0
            )
        val angleIncrement = 360.0 / sections
        val angle = Math.toRadians((circleLoc * angleIncrement))
        val particleX = origin.x + cos(angle) * 20
        val particleZ = origin.z + sin(angle) * 20
        val loc = Transform(particleX, origin.position.y, particleZ, angle, 0.0, origin.level)
        if (entity.position.distance(loc.position) < 3) {
            circleLoc++
            circleLoc %= 8
        }
        setRouteTarget(entity, loc.position)
        setLookTarget(entity, loc.position)
        if (updateRouteImmediatelyWhenTargetChange) {
            val floor = target.floor()

            if (oldTarget == null || oldTarget == floor) entity.behaviorGroup.isForceUpdateRoute = (true)

            oldTarget = floor
        }

        if (entity.movementSpeed != speed) entity.movementSpeed = speed

        return true
    }

    override fun onStart(entity: EntityMob) {
        entity.memoryStorage.set(CoreMemoryTypes.ENABLE_PITCH, false)
    }

    override fun onInterrupt(entity: EntityMob) {
        entity.movementSpeed = EntityLiving.DEFAULT_SPEED
        entity.memoryStorage.set(CoreMemoryTypes.ENABLE_PITCH, true)
        entity.isEnablePitch = false
        if (clearDataWhenLose) entity.behaviorGroup.memoryStorage.clear(memory)
        circleLoc++
        circleLoc %= 8
    }

    override fun onStop(entity: EntityMob) {
        entity.movementSpeed = EntityLiving.DEFAULT_SPEED
        entity.memoryStorage.set(CoreMemoryTypes.ENABLE_PITCH, true)
        entity.isEnablePitch = false
        if (clearDataWhenLose) entity.behaviorGroup.memoryStorage.clear(memory)
        circleLoc++
        circleLoc %= 8
    }
}
