package org.chorus_oss.chorus.entity.ai.evaluator

import org.chorus_oss.chorus.entity.mob.EntityMob

/**
 * 全部行为评估通过才执行的一个评估器.
 *
 *
 * An evaluator that is executed only after all behaviors have been evaluated.
 */
open class AllMatchEvaluator : MultiBehaviorEvaluator {
    constructor(evaluators: Set<IBehaviorEvaluator>) : super(evaluators)

    constructor(vararg evaluators: IBehaviorEvaluator) : super(*evaluators)

    override fun evaluate(entity: EntityMob): Boolean {
        for (evaluator in evaluators) {
            if (!evaluator.evaluate(entity)) return false
        }
        return true
    }
}
