package org.chorus_oss.chorus.entity.ai.evaluator

import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.math.Vector3

class BlockCheckEvaluator(protected var blockId: String, protected var offsetVec: Vector3) : IBehaviorEvaluator {
    override fun evaluate(entity: EntityMob): Boolean {
        return entity.level!!.getTickCachedBlock(entity.position.add(offsetVec)).id == blockId
    }
}
