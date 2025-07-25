package org.chorus_oss.chorus.entity.ai.evaluator

import org.chorus_oss.chorus.entity.mob.EntityMob


class NotMatchEvaluator(
    val evaluator: IBehaviorEvaluator
) : IBehaviorEvaluator {
    override fun evaluate(entity: EntityMob): Boolean {
        return !evaluator.evaluate(entity)
    }
}
