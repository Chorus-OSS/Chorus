package org.chorus_oss.chorus.entity.ai.behaviorgroup

import org.chorus_oss.chorus.entity.ai.behavior.IBehavior
import org.chorus_oss.chorus.entity.ai.controller.IController
import org.chorus_oss.chorus.entity.ai.memory.IMemoryStorage
import org.chorus_oss.chorus.entity.ai.memory.MemoryStorage
import org.chorus_oss.chorus.entity.ai.route.finder.IRouteFinder
import org.chorus_oss.chorus.entity.ai.sensor.ISensor
import org.chorus_oss.chorus.entity.mob.EntityMob

/**
 * 用于未实现AI的实体，作为占位符使用
 */
class EmptyBehaviorGroup(entity: EntityMob) : IBehaviorGroup {
    override var memoryStorage: IMemoryStorage = MemoryStorage(entity)

    override fun evaluateBehaviors(entity: EntityMob) {}

    override fun evaluateCoreBehaviors(entity: EntityMob) {}

    override fun collectSensorData(entity: EntityMob) {}

    override fun tickRunningBehaviors(entity: EntityMob) {}

    override fun tickRunningCoreBehaviors(entity: EntityMob) {}

    override fun applyController(entity: EntityMob) {}

    override fun getBehaviors(): Set<IBehavior> {
        return setOf()
    }

    override fun getCoreBehaviors(): Set<IBehavior> {
        return setOf()
    }

    override fun getRunningBehaviors(): Set<IBehavior> {
        return setOf()
    }

    override fun getRunningCoreBehaviors(): Set<IBehavior> {
        return setOf()
    }

    override fun getSensors(): Set<ISensor> {
        return setOf()
    }

    override fun getControllers(): Set<IController> {
        return setOf()
    }

    override fun getRouteFinder(): IRouteFinder? {
        return null
    }

    override fun updateRoute(entity: EntityMob) {
    }

    override var isForceUpdateRoute: Boolean = false
        set(value) {
            throw UnsupportedOperationException("Cannot set isForceUpdateRoute")
        }
}
