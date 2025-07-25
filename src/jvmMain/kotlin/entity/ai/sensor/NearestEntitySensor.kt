package org.chorus_oss.chorus.entity.ai.sensor

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.ai.memory.NullableMemoryType
import org.chorus_oss.chorus.entity.mob.EntityMob


//存储最近的玩家的Memory

class NearestEntitySensor @JvmOverloads constructor(
    protected var entityClass: Class<out Entity>,
    protected var memoryType: NullableMemoryType<Entity>,
    protected var range: Double,
    protected var minRange: Double,
    override var period: Int = 1
) :
    ISensor {
    override fun sense(entity: EntityMob) {
        var ent: Entity? = null
        val rangeSquared = this.range * this.range
        val minRangeSquared = this.minRange * this.minRange
        //寻找范围内最近的玩家
        for (e in entity.level!!.entities.values) {
            if (entityClass.isAssignableFrom(e.javaClass)) {
                if (entity.position.distanceSquared(e.position) <= rangeSquared && entity.position.distanceSquared(e.position) >= minRangeSquared) {
                    if (ent == null) {
                        ent = e
                    } else {
                        if (entity.position.distanceSquared(e.position) < entity.position.distanceSquared(ent.position)) {
                            ent = e
                        }
                    }
                }
            }
        }
        if (ent == null) {
            if (entity.memoryStorage.notEmpty(memoryType) && entity.memoryStorage[memoryType]!!.javaClass.isAssignableFrom(
                    entityClass
                )
            ) {
                entity.memoryStorage.clear(memoryType)
            } // We don't want to clear data from different sensors
        } else entity.memoryStorage[memoryType] = ent
    }
}
