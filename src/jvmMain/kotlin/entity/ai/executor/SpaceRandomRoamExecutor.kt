package org.chorus_oss.chorus.entity.ai.executor

import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.math.Vector3
import java.util.concurrent.ThreadLocalRandom

/**
 * 3D随机漫游
 */
class SpaceRandomRoamExecutor @JvmOverloads constructor(
    speed: Float,
    maxXZRoamRange: Int,
    protected var maxYRoamRange: Int,
    frequency: Int,
    calNextTargetImmediately: Boolean = false,
    runningTime: Int = 100,
    avoidWater: Boolean = true,
    maxRetryTime: Int = 10
) :
    FlatRandomRoamExecutor(
        speed,
        maxXZRoamRange,
        frequency,
        calNextTargetImmediately,
        runningTime,
        avoidWater,
        maxRetryTime
    ) {
    override fun next(entity: EntityMob): Vector3 {
        val random = ThreadLocalRandom.current()
        val x = random.nextInt(maxRoamRange * 2) - maxRoamRange + entity.position.floorX
        val z = random.nextInt(maxRoamRange * 2) - maxRoamRange + entity.position.floorZ
        val y = random.nextInt(maxYRoamRange * 2) - maxYRoamRange + entity.position.floorY
        return Vector3(x.toDouble(), y.toDouble(), z.toDouble())
    }
}
