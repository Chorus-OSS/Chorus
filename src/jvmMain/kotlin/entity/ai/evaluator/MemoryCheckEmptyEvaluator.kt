package org.chorus_oss.chorus.entity.ai.evaluator

import org.chorus_oss.chorus.entity.ai.memory.NullableMemoryType
import org.chorus_oss.chorus.entity.mob.EntityMob

class MemoryCheckEmptyEvaluator(private val type: NullableMemoryType<*>) : IBehaviorEvaluator {
    override fun evaluate(entity: EntityMob): Boolean {
        return entity.behaviorGroup.memoryStorage.isEmpty(type)
    }
}
