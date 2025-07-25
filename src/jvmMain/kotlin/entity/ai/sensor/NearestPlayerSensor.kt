package org.chorus_oss.chorus.entity.ai.sensor

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.mob.EntityMob


//存储最近的玩家的Memory

open class NearestPlayerSensor @JvmOverloads constructor(
    protected var range: Double,
    protected var minRange: Double,
    override var period: Int = 1
) :
    ISensor {
    override fun sense(entity: EntityMob) {
        var player: Player? = null
        val rangeSquared = this.range * this.range
        val minRangeSquared = this.minRange * this.minRange
        //寻找范围内最近的玩家
        for (p in entity.level!!.players.values) {
            if (entity.position.distanceSquared(p.position) <= rangeSquared && entity.position.distanceSquared(p.position) >= minRangeSquared) {
                if (player == null) {
                    player = p
                } else {
                    if (entity.position.distanceSquared(p.position) < entity.position.distanceSquared(player.position)) {
                        player = p
                    }
                }
            }
        }
        entity.memoryStorage[CoreMemoryTypes.NEAREST_PLAYER] = player
    }
}
