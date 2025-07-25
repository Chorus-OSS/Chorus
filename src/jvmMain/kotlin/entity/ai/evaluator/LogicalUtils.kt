package org.chorus_oss.chorus.entity.ai.evaluator

/**
 * 提供部分实用方法封装
 * <br></br>
 * Provide some utility method encapsulation
 */
interface LogicalUtils {
    fun any(evaluators: Set<IBehaviorEvaluator>): IBehaviorEvaluator {
        return AnyMatchEvaluator(evaluators)
    }

    fun any(vararg evaluators: IBehaviorEvaluator): IBehaviorEvaluator {
        return AnyMatchEvaluator(*evaluators)
    }

    fun all(evaluators: Set<IBehaviorEvaluator>): IBehaviorEvaluator {
        return AllMatchEvaluator(evaluators)
    }

    fun all(vararg evaluators: IBehaviorEvaluator): IBehaviorEvaluator {
        return AllMatchEvaluator(*evaluators)
    }

    fun not(evaluator: IBehaviorEvaluator): IBehaviorEvaluator {
        return NotMatchEvaluator(evaluator)
    }

    fun none(): IBehaviorEvaluator {
        return IBehaviorEvaluator { true }
    }
}
