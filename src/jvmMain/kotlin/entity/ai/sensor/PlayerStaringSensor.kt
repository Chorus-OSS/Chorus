package org.chorus_oss.chorus.entity.ai.sensor

import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.mob.EntityMob
import kotlin.math.abs


open class PlayerStaringSensor(
    protected var range: Double,
    protected var triggerDiff: Double,
    protected var ignoreRotation: Boolean
) :
    ISensor {
    override fun sense(entity: EntityMob) {
        for (player in entity.viewers.values) {
            if (player.position.distance(entity.position) <= range) {
                if (ignoreRotation || abs(abs(player.headYaw - entity.headYaw) - 180) <= this.triggerDiff) {
                    if (player.isLookingAt(
                            entity.position.add(0.0, entity.getEyeHeight().toDouble(), 0.0),
                            triggerDiff,
                            true
                        )
                    ) {
                        entity.memoryStorage[CoreMemoryTypes.STARING_PLAYER] = player
                        return
                    }
                }
            }
        }
        entity.memoryStorage.clear(CoreMemoryTypes.STARING_PLAYER)
    }
}
