package org.chorus_oss.chorus.entity.ai.evaluator

import org.chorus_oss.chorus.entity.mob.EntityMob

class AnyMatchEvaluator : MultiBehaviorEvaluator {
    constructor(evaluators: Set<IBehaviorEvaluator>) : super(evaluators)

    constructor(vararg evaluators: IBehaviorEvaluator) : super(*evaluators)

    override fun evaluate(entity: EntityMob): Boolean {
        for (evaluator in evaluators) {
            if (evaluator.evaluate(entity)) return true
        }
        return false
    }
}
