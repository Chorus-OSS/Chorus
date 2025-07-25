package org.chorus_oss.chorus.entity.ai.executor

import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.math.Vector3
import kotlin.random.Random

class HomeboundSpaceRandomRoamExecutor(
    speed: Float,
    maxXZRoamRange: Int,
    val maxYRoamRange: Int,
    frequency: Int,
    callNextTargetImmediately: Boolean = false,
    runningTime: Int = 100,
    avoidWater: Boolean = true,
    maxRetryTime: Int = 10,
    val home: Vector3,
    val homeRadius: Int,
) : FlatRandomRoamExecutor(
    speed,
    maxXZRoamRange,
    frequency,
    callNextTargetImmediately,
    runningTime,
    avoidWater,
    maxRetryTime
) {
    override fun next(entity: EntityMob): Vector3 {
        val x = Random.nextInt(homeRadius * 2) - homeRadius + home.floorX
        val z = Random.nextInt(homeRadius * 2) - homeRadius + home.floorZ
        val y = Random.nextInt(maxYRoamRange * 2) - maxYRoamRange + home.floorY
        if (x !in (home.floorX - homeRadius)..(home.floorX + homeRadius) || z !in (home.floorZ - homeRadius)..(home.floorZ + homeRadius)) {
            return next(entity)
        }
        return Vector3(x.toDouble(), y.toDouble(), z.toDouble())
    }
}