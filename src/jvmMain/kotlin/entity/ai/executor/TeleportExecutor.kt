package org.chorus_oss.chorus.entity.ai.executor


import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.level.Locator
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.math.Vector3
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.floor
import kotlin.math.min

class TeleportExecutor(
    var maxDistance: Int = 0,
    var minDistance: Int = 0,
    var maxTries: Int = 16,
) : IBehaviorExecutor {
    private fun find(locator: Locator): Locator {
        val distance = maxDistance - minDistance
        val dx = locator.position.x + ThreadLocalRandom.current().nextInt(-distance, distance) + minDistance
        val dz = locator.position.z + ThreadLocalRandom.current().nextInt(-distance, distance) + minDistance
        val pos = Vector3(floor(dx), (floor(locator.position.y + 0.1).toInt() + maxDistance).toDouble(), floor(dz))
        for (y in min(
            locator.level.maxHeight,
            pos.y.toInt()
        ) downTo locator.level.minHeight + 1) {
            val block = locator.level.getBlock(dx.toInt(), y, dz.toInt())
            if (block.isSolid) {
                return block.up().locator
            }
        }
        return locator
    }

    override fun execute(entity: EntityMob): Boolean {
        var locator = entity.locator
        for (i in 0..<maxTries) {
            if (locator.position.distance(entity.position) < minDistance) {
                locator = find(entity.transform)
            } else break
        }
        if (entity.position.distance(locator.position) > minDistance) {
            entity.teleport(locator)
            locator.level.addSound(locator.position, Sound.MOB_ENDERMEN_PORTAL)
        }
        return true
    }
}
