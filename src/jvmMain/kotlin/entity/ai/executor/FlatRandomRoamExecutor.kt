package org.chorus_oss.chorus.entity.ai.executor

import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.math.Vector3
import java.util.concurrent.ThreadLocalRandom

open class FlatRandomRoamExecutor @JvmOverloads constructor(
    protected var speed: Float,
    protected var maxRoamRange: Int,
    protected var frequency: Int,
    protected var calNextTargetImmediately: Boolean = false,
    protected var runningTime: Int = 100,
    protected var avoidWater: Boolean = false,
    protected var maxRetryTime: Int = 10
) :
    EntityControl, IBehaviorExecutor {
    protected var currentTargetCalTick: Int
    protected var durationTick: Int = 0

    /**
     * Instantiates a new Flat random roam executor.
     *
     * @param speed                    移动速度<br></br>Movement speed
     * @param maxRoamRange             随机行走目标点的范围<br></br>The range of the target point that is randomly walked
     * @param frequency                更新目标点的频率<br></br>How often the target point is updated
     * @param calNextTargetImmediately 是否立即选择下一个目标点,不管执行频率<br></br>Whether to select the next target point immediately, regardless of the frequency of execution
     * @param runningTime              执行最大的用时,-1代表不限制<br></br>Maximum time to execute,-1 means no limit
     * @param avoidWater               是否避开水行走<br></br>Whether to walk away from water
     * @param maxRetryTime             选取目标点的最大尝试次数<br></br>Pick the maximum number of attempts at the target point
     */
    init {
        this.currentTargetCalTick = this.frequency
    }

    override fun execute(entity: EntityMob): Boolean {
        currentTargetCalTick++
        durationTick++
        if (entity.isEnablePitch) entity.isEnablePitch = false
        if (currentTargetCalTick >= frequency || (calNextTargetImmediately && needUpdateTarget(entity))) {
            var target = next(entity)
            if (avoidWater) {
                var blockId: String
                var time = 0
                while (time <= maxRetryTime && ((entity.level!!.getTickCachedBlock(
                        target.add(
                            0.0,
                            -1.0,
                            0.0
                        )
                    ).id.also { blockId = it }) === BlockID.FLOWING_WATER || blockId === BlockID.WATER)
                ) {
                    target = next(entity)
                    time++
                }
            }
            if (entity.movementSpeed != speed) entity.movementSpeed = speed
            //更新寻路target
            setRouteTarget(entity, target)
            //更新视线target
            setLookTarget(entity, target)
            currentTargetCalTick = 0
            entity.behaviorGroup.isForceUpdateRoute = calNextTargetImmediately
        }
        if (durationTick <= runningTime || runningTime == -1) return true
        else {
            currentTargetCalTick = 0
            durationTick = 0
            return false
        }
    }

    override fun onInterrupt(entity: EntityMob) {
        stop(entity)
    }

    override fun onStop(entity: EntityMob) {
        stop(entity)
    }

    protected fun stop(entity: EntityMob) {
        removeRouteTarget(entity)
        removeLookTarget(entity)
        entity.isEnablePitch = true
        currentTargetCalTick = 0
        durationTick = 0
    }

    protected fun needUpdateTarget(entity: EntityMob): Boolean {
        return entity.moveTarget == null
    }

    protected open fun next(entity: EntityMob): Vector3 {
        val random = ThreadLocalRandom.current()
        val x = random.nextInt(maxRoamRange * 2) - maxRoamRange + entity.position.floorX
        val z = random.nextInt(maxRoamRange * 2) - maxRoamRange + entity.position.floorZ
        return Vector3(x.toDouble(), entity.position.y, z.toDouble())
    }
}
