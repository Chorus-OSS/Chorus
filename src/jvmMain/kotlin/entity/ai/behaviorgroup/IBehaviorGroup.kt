package org.chorus_oss.chorus.entity.ai.behaviorgroup

import org.chorus_oss.chorus.entity.ai.behavior.IBehavior
import org.chorus_oss.chorus.entity.ai.controller.IController
import org.chorus_oss.chorus.entity.ai.memory.IMemoryStorage
import org.chorus_oss.chorus.entity.ai.route.finder.IRouteFinder
import org.chorus_oss.chorus.entity.ai.sensor.ISensor
import org.chorus_oss.chorus.entity.mob.EntityMob

/**
 * 行为组是一个基本的、独立的AI单元<br></br>
 * 它由若干个（核心）行为[IBehavior]、控制器[IController]、传感器[ISensor]以及一个寻路器[IRouteFinder]和记忆存储器[IMemoryStorage]组成br>
 * 注：核心行为指的是不会被行为优先级影响的行为，其激活状态只取决于其自身的评估器br>
 *
 *
 * A Behavior Group is a basic, self-contained unit of AI<br></br>
 * that consists of several (core) Behaviors[IBehavior], Controllers[IController], Sensors[ISensor] and a Pathfinder[IRouteFinder] and memory [IMemoryStorage] are composed<br></br>
 * Note: Core behavior refers to the behavior that will not be affected by the priority of the behavior, and its activation status only depends on its own evaluator
 */
interface IBehaviorGroup {
    val memoryStorage: IMemoryStorage

    /**
     * 调用行为组内部的所有行为[IBehavior]的评估器[org.chorus_oss.chorus.entity.ai.evaluator.IBehaviorEvaluator]
     *
     *
     * Call the evaluator [org.chorus_oss.chorus.entity.ai.evaluator.IBehaviorEvaluator] of all behavior [IBehavior] inside the behavior group
     *
     * @param entity 目标实体对象
     */
    fun evaluateBehaviors(entity: EntityMob)

    /**
     * 调用行为组内部的所有核心行为[IBehavior]的评估器[org.chorus_oss.chorus.entity.ai.evaluator.IBehaviorEvaluator]
     *
     *
     * Call the evaluator [org.chorus_oss.chorus.entity.ai.evaluator.IBehaviorEvaluator] of all core behavior [IBehavior] inside the behavior group
     *
     * @param entity 目标实体对象
     */
    fun evaluateCoreBehaviors(entity: EntityMob)

    /**
     * 调用行为组内部的所有传感器[ISensor]，并将传感器返回的记忆数据写入到记忆存储器中[IMemoryStorage]
     *
     *
     * Call all sensors [ISensor] inside the behavior group, and write the memory data returned by the sensor to the memory storage [IMemoryStorage]
     *
     * @param entity 目标实体对象
     */
    fun collectSensorData(entity: EntityMob)

    /**
     * 调用行为组内部所有被激活的行为[IBehavior]的执行器[org.chorus_oss.chorus.entity.ai.executor.IBehaviorExecutor]
     *
     *
     * Call the executor [org.chorus_oss.chorus.entity.ai.executor.IBehaviorExecutor] of all activated behavior [IBehavior] inside the behavior group
     *
     * @param entity 目标实体对象
     */
    fun tickRunningBehaviors(entity: EntityMob)

    /**
     * 调用行为组内部所有被激活的核心行为[IBehavior]的执行器[org.chorus_oss.chorus.entity.ai.executor.IBehaviorExecutor]
     *
     *
     * Call the executor [org.chorus_oss.chorus.entity.ai.executor.IBehaviorExecutor] of all activated core behavior [IBehavior] inside the behavior group
     *
     * @param entity 目标实体对象
     */
    fun tickRunningCoreBehaviors(entity: EntityMob)

    /**
     * 应用行为内部所有的控制器[IController]
     *
     *
     * All controllers inside the application behavior[IController]
     *
     * @param entity 目标实体对象
     */
    fun applyController(entity: EntityMob)

    /**
     * @return 行为组包含的行为 [IBehavior]<br></br>Behaviors contained in Behavior Groups [IBehavior]
     */
    fun getBehaviors(): Set<IBehavior>

    /**
     * @return 行为组包含的核心行为 [IBehavior]<br></br>Core Behaviors Contained by Behavior Groups [IBehavior]
     */
    fun getCoreBehaviors(): Set<IBehavior>

    /**
     * @return 被激活的行为 [IBehavior]<br></br>Activated Behavior [IBehavior]
     */
    fun getRunningBehaviors(): Set<IBehavior>

    /**
     * @return 被激活的核心行为 [IBehavior]<br></br>Activated Core Behavior [IBehavior]
     */
    fun getRunningCoreBehaviors(): Set<IBehavior>

    /**
     * @return 行为组包含的传感器 [ISensor]<br></br>Behavior group includes sensors [ISensor]
     */
    fun getSensors(): Set<ISensor>

    /**
     * @return 行为组包含的控制器 [IController]<br></br>Behavior group contains the controller [IController]
     */
    fun getControllers(): Set<IController>

    /**
     * @return 行为组使用的寻路器 [IRouteFinder]<br></br>Routefinder used by behavior groups [IRouteFinder]
     */
    fun getRouteFinder(): IRouteFinder?

    /**
     * 通过行为组使用的寻路器更新当前位置到目标位置路径
     *
     *
     * Update the path from the current position to the target position through the pathfinder used by the behavior group
     *
     * @param entity 目标实体
     */
    fun updateRoute(entity: EntityMob)

    var isForceUpdateRoute: Boolean

    /**
     * 当 EntityAI.checkDebugOption(BEHAVIOR) == true 时此方法每1gt调用一次，用于debug模式相关内容的刷新
     *
     *
     * When EntityAI.checkDebugOption(BEHAVIOR) == true, this method is called every 1gt to refresh the content related to debug mode
     */
    fun debugTick(entity: EntityMob) {
    }

    fun save(entity: EntityMob) {
        //EmptyBehaviorGroup will return null
        memoryStorage.encode()
    }
}
