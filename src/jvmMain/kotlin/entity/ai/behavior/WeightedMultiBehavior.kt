package org.chorus_oss.chorus.entity.ai.behavior


import org.chorus_oss.chorus.entity.ai.behaviorgroup.IBehaviorGroup
import org.chorus_oss.chorus.entity.mob.EntityMob
import java.util.concurrent.ThreadLocalRandom

/**
 * 由多个行为[IBehavior]组成的组（注意和行为组[IBehaviorGroup]区分）<br></br>
 * 调用方法[org.chorus_oss.chorus.entity.ai.executor.IBehaviorExecutor.execute]前，必须调用此对象的评估函数以确认激活的是哪个行为<br></br>
 * 在评估时，会评估所有包含的子行为<br></br>
 * 筛选出返回成功的行为后，会选取最高优先级的那一组<br></br>
 * 如果到这一步依然存在多个行为，则会根据行为的[IBehavior.getWeight]方法的返回值随机选取其中一个作为执行行为
 *
 *
 * A group consisting of multiple behaviors [IBehavior] (note the distinction with behavior groups [IBehaviorGroup])<br></br>
 * Before calling the method [org.chorus_oss.chorus.entity.ai.executor.IBehaviorExecutor.execute], the evaluation function of this object must be called to confirm which behavior is activated<br></br>
 * During evaluation, all contained child behaviors are evaluated<br></br>
 * After filtering out the behaviors that return success, the group with the highest priority is selected<br></br>
 * If there are still multiple behaviors at this point, one of them is randomly selected for execution based on the return value of the [IBehavior.getWeight] method of the behavior
 */

class WeightedMultiBehavior : AbstractBehavior {
    /**
     * 此组的优先级。在BehaviorGroup中，获取优先级将会返回此值指代整个组的优先级
     *
     *
     * The priority of this group. In Behavior Group, getting the priority will return this value to refer to the priority of the entire group
     */
    override val priority: Int
    protected var behaviors: Set<IBehavior>


    protected var currentBehavior: IBehavior? = null

    constructor(priority: Int, vararg behaviors: IBehavior) {
        this.priority = priority
        this.behaviors = setOf(*behaviors)
    }

    constructor(priority: Int, behaviors: Set<IBehavior>) {
        this.priority = priority
        this.behaviors = behaviors
    }

    override fun evaluate(entity: EntityMob): Boolean {
        val result = evaluateBehaviors(entity)
        if (result.isEmpty()) {
            return false
        }
        if (result.size == 1) {
            currentBehavior = (result.iterator().next())
            return true
        }
        //根据Weight选取一个行为
        var totalWeight = 0
        for (behavior in result) {
            totalWeight += behavior.weight
        }
        var random = ThreadLocalRandom.current().nextInt(totalWeight + 1)
        for (behavior in result) {
            random -= behavior.weight
            if (random <= 0) {
                currentBehavior = (behavior)
                return true
            }
        }
        return false
    }

    override fun execute(entity: EntityMob): Boolean {
        if (currentBehavior == null) {
            return false
        }
        return currentBehavior!!.execute(entity)
    }

    override fun onInterrupt(entity: EntityMob) {
        if (currentBehavior == null) {
            return
        }
        currentBehavior!!.onInterrupt(entity)
        currentBehavior!!.behaviorState = (BehaviorState.STOP)
    }

    override fun onStart(entity: EntityMob) {
        if (currentBehavior == null) {
            return
        }
        currentBehavior!!.onStart(entity)
        currentBehavior!!.behaviorState = (BehaviorState.ACTIVE)
    }

    override fun onStop(entity: EntityMob) {
        if (currentBehavior == null) {
            return
        }
        currentBehavior!!.onStop(entity)
        currentBehavior!!.behaviorState = (BehaviorState.STOP)
    }

    /**
     * @param entity 实体
     * @return 最高优先级且评估成功的一组行为（包含评估结果）
     */
    protected fun evaluateBehaviors(entity: EntityMob?): Set<IBehavior> {
        //存储评估成功的行为（未过滤优先级）
        val evalSucceed = HashSet<IBehavior>()
        var highestPriority = Int.MIN_VALUE
        for (behavior in behaviors) {
            if (behavior.evaluate(entity!!)) {
                evalSucceed.add(behavior)
                if (behavior.priority > highestPriority) {
                    highestPriority = behavior.priority
                }
            }
        }
        //如果没有评估结果，则返回空
        if (evalSucceed.isEmpty()) {
            return evalSucceed
        }
        //过滤掉低优先级的行为
        val result = HashSet<IBehavior>()
        for (entry in evalSucceed) {
            if (entry.priority == highestPriority) {
                result.add(entry)
            }
        }
        return result
    }
}
