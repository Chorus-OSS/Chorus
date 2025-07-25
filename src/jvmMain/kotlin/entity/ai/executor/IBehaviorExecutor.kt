package org.chorus_oss.chorus.entity.ai.executor

import org.chorus_oss.chorus.entity.mob.EntityMob

/**
 * 行为执行器<br></br>
 * 在实体上执行具体的行为<br></br>
 * 对于每个实例化的实体，此对象应只会实例化一次，且一直伴随实体不会改变
 *
 *
 * Behavior executor<br></br>
 * Executes specific behavior on the entity<br></br>
 * For each instantiated entity, this object should only be instantiated once, and the entity will not change all the time
 */
fun interface IBehaviorExecutor {
    /**
     * 调度器将会持续执行此执行器，直到返回false，或者执行器被中断<br></br>
     * 此方法每gt都会调用
     *
     *
     * The scheduler will continue to execute this executor until it returns false or the executor is interrupted<br></br>
     * This method will be called every gt
     *
     * @param entity 执行目标实体
     * @return boolean
     */
    fun execute(entity: EntityMob): Boolean

    /**
     * 行为非正常中断时(例如被更高级行为覆盖)调用
     *
     *
     * Called when behavior breaks abnormally (e.g. overridden by higher-level behavior)
     *
     * @param entity 目标实体
     */
    fun onInterrupt(entity: EntityMob) {}

    /**
     * 行为评估成功后，进入激活状态前调用
     *
     *
     * After the behavior evaluation is successful, it is called before entering the active state
     *
     * @param entity 目标实体
     */
    fun onStart(entity: EntityMob) {}

    /**
     * 行为正常结束时(execute()方法返回false)调用
     *
     *
     * Called when the behavior ends normally (the execute() method returns false)
     *
     * @param entity 目标实体
     */
    fun onStop(entity: EntityMob) {}
}
