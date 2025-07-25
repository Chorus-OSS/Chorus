package org.chorus_oss.chorus.entity.ai.evaluator

import org.chorus_oss.chorus.entity.ai.memory.IMemoryType
import org.chorus_oss.chorus.entity.ai.memory.MemoryType
import org.chorus_oss.chorus.entity.ai.memory.NullableMemoryType
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.math.IVector3

class DistanceEvaluator @JvmOverloads constructor(
    private val type: IMemoryType<out IVector3?>,
    private val maxDistance: Double,
    private val minDistance: Double = -1.0
) :
    IBehaviorEvaluator {
    override fun evaluate(entity: EntityMob): Boolean {
        return when (type) {
            is MemoryType -> {
                val location = entity.memoryStorage[type]!!.vector3
                val distance = entity.position.distance(location)
                distance in minDistance..maxDistance
            }

            is NullableMemoryType -> {
                if (entity.memoryStorage.isEmpty(type)) {
                    false
                } else {
                    val location = entity.memoryStorage[type]!!.vector3
                    val distance = entity.position.distance(location)
                    distance in minDistance..maxDistance
                }
            }
        }
    }
}
