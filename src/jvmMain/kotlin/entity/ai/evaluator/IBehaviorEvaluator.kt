package org.chorus_oss.chorus.entity.ai.evaluator

import org.chorus_oss.chorus.entity.mob.EntityMob

/**
 * 此接口抽象了一个行为评估器 <br></br> 决定是否激活与其绑定的执行器
 *
 *
 * This interface abstracts a Behavior Evaluator that <br></br> decides whether to activate the Actuator bound to it
 */
fun interface IBehaviorEvaluator {
    /**
     * 是否需要激活与其绑定的执行器
     *
     *
     * 这个方法对一个行为只会评估一次，评估通过则开始运行执行器执行行为，直到行为中断或者完成，下一次评估才会开始
     *
     *
     * Whether the executor bound to it needs to be activated
     *
     *
     * This method evaluates a behavior only once, and if the evaluation passes, the executor execution behavior will start running until the behavior is interrupted or completed, and the next evaluation will not begin
     *
     * @param entity 评估目标实体<br></br>Assess the targetEntity
     * @return 是否需要激活<br></br>Do you need to activate
     */
    fun evaluate(entity: EntityMob): Boolean
}
