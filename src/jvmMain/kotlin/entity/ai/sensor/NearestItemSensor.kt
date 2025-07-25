package org.chorus_oss.chorus.entity.ai.sensor

import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.item.EntityItem
import org.chorus_oss.chorus.entity.mob.EntityMob


//存储最近的玩家的Memory

class NearestItemSensor @JvmOverloads constructor(
    protected var range: Double,
    protected var minRange: Double,
    override var period: Int = 1
) :
    ISensor {
    override fun sense(entity: EntityMob) {
        val itemClass = entity.memoryStorage[CoreMemoryTypes.LOOKING_ITEM]!!
        var item: EntityItem? = null
        val rangeSquared = this.range * this.range
        val minRangeSquared = this.minRange * this.minRange
        //寻找范围内最近的玩家
        for (e in entity.level!!.entities.values) {
            if (e is EntityItem) {
                if (itemClass.isAssignableFrom(e.item.javaClass)) {
                    if (entity.position.distanceSquared(e.position) <= rangeSquared && entity.position.distanceSquared(e.position) >= minRangeSquared) {
                        if (item == null) {
                            item = e
                        } else {
                            if (entity.position.distanceSquared(e.position) < entity.position.distanceSquared(item.position)) {
                                item = e
                            }
                        }
                    }
                }
            }
        }
        entity.memoryStorage[CoreMemoryTypes.NEAREST_ITEM] = item
    }
}
