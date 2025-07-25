package org.chorus_oss.chorus.entity.ai.behavior

import org.chorus_oss.chorus.entity.ai.behaviorgroup.IBehaviorGroup
import org.chorus_oss.chorus.entity.ai.evaluator.IBehaviorEvaluator
import org.chorus_oss.chorus.entity.ai.executor.IBehaviorExecutor

/**
 * 此接口抽象了一个行为对象，作为行为组[IBehaviorGroup]的组成部分.
 *
 *
 * This interface abstracts a behavior object as part of the behavior group [IBehaviorGroup].
 */
interface IBehavior : IBehaviorExecutor, IBehaviorEvaluator {
    val priority: Int
        /**
         * 返回此行为的优先级，高优先级的行为会覆盖低优先级的行为
         *
         *
         * Returns the priority of this behavior, with higher-priority behaviors overriding lower-priority ones
         *
         * @return 优先级
         */
        get() = 1

    val weight: Int
        /**
         * 返回此行为的权重值，高权重的行为有更大几率被选中
         *
         *
         * Returns the weight value of the behavior, with higher weights having a higher chance of being selected
         *
         * @return 权重值
         */
        get() = 1

    val period: Int
        /**
         * 返回此行为的刷新周期，小的刷新周期会使得评估器被更频繁的调用。注意此方法只会影响评估器的调用，而不会影响执行器的调用。
         *
         *
         * Returns the refresh period for this behavior, a small refresh period will cause the evaluator to be called more often. Note that this method only affects evaluator calls, not executor calls.
         *
         * @return 刷新周期<br></br>Refresh period
         */
        get() = 1

    /**
     * @return 此行为当前的状态<br></br>The current state of this behavior
     */
    /**
     * 设置此行为的状态
     *
     *
     * Set the status of this behavior
     *
     * @param state 状态
     */
    var behaviorState: BehaviorState?
}
