package org.chorus_oss.chorus.entity.ai.executor

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.mob.EntityMob

open class EntityBreedingExecutor<T : EntityMob>(
    protected var entityClass: Class<out T>,
    protected var findingRangeSquared: Int,
    protected var duration: Int,
    protected var moveSpeed: Float
) :
    IBehaviorExecutor {
    protected var currentTick: Int = 0
    protected var finded: Boolean = false
    protected var another: T? = null

    override fun execute(uncasted: EntityMob): Boolean {
        if (entityClass.isInstance(uncasted)) {
            val entity = entityClass.cast(uncasted)
            if (shouldFindingSpouse(entity)) {
                if (!entity!!.memoryStorage[CoreMemoryTypes.IS_IN_LOVE]) return false
                another = getNearestInLove(entity)
                if (another == null) return true
                setSpouse(entity, another!!)

                //set move speed
                entity.movementSpeed = moveSpeed
                another!!.movementSpeed = moveSpeed

                finded = true
            }
            if (finded) {
                currentTick++

                updateMove(entity, another)

                if (currentTick > duration) {
                    bear(entity)
                    clearData(entity)
                    clearData(another!!)

                    currentTick = 0
                    finded = false
                    entity!!.isEnablePitch = false
                    another!!.isEnablePitch = false
                    another = null
                    return false
                }
            }
            return true
        } else {
            return false
        }
    }

    override fun onInterrupt(entity: EntityMob) {
        clearData(entity as T)

        currentTick = 0
        finded = false
        entity.isEnablePitch = false
        if (another != null) {
            clearData(another!!)
            another!!.isEnablePitch = false
            another = null
        }
    }

    protected fun setSpouse(entity1: T, entity2: T) {
        entity1.memoryStorage.set(CoreMemoryTypes.ENTITY_SPOUSE, entity2)
        entity2.memoryStorage.set(CoreMemoryTypes.ENTITY_SPOUSE, entity1)
    }

    protected open fun clearData(entity: T) {
        entity.memoryStorage.clear(CoreMemoryTypes.ENTITY_SPOUSE)
        //clear move target
        entity.moveTarget = null
        //clear look target
        entity.lookTarget = null
        //reset move speed
        entity.setMovementSpeedF(0.1f)
        //interrupt in love status
        entity.memoryStorage.set(CoreMemoryTypes.IS_IN_LOVE, false)
    }

    protected fun updateMove(entity1: T, entity2: T?) {
        if (!entity1.isEnablePitch) entity1.isEnablePitch = true
        if (!entity2!!.isEnablePitch) entity2.isEnablePitch = true

        //已经挨在一起了就不用更新路径了
        //If they are already close together, there is no need to update the path
        if (entity1.offsetBoundingBox.intersectsWith(entity2.offsetBoundingBox)) return

        //clone the vec
        val cloned1 = entity1.position.clone()
        val cloned2 = entity2.position.clone()

        //update move target
        entity1.moveTarget = cloned2
        entity2.moveTarget = cloned1

        //update look target
        entity1.lookTarget = cloned2
        entity2.lookTarget = cloned1

        //在下一gt立即更新路径
        //Immediately update the path on the next gt
        entity1.behaviorGroup.isForceUpdateRoute = true
        entity2.behaviorGroup.isForceUpdateRoute = true
    }

    protected fun getNearestInLove(entity: EntityMob): T? {
        val entities = entity.level!!.entities
        var maxDistanceSquared = -1.0
        var nearestInLove: T? = null
        for (e in entities.values) {
            val newDistance = e.position.distanceSquared(entity.position)
            if (e != entity && entityClass.isInstance(e)) {
                val another = e as T
                if (!another.isBaby() && another.memoryStorage[CoreMemoryTypes.IS_IN_LOVE] && another.memoryStorage
                        .isEmpty(CoreMemoryTypes.ENTITY_SPOUSE) && (maxDistanceSquared == -1.0 || newDistance < maxDistanceSquared)
                ) {
                    maxDistanceSquared = newDistance
                    nearestInLove = another
                }
            }
        }
        return nearestInLove
    }

    protected fun shouldFindingSpouse(entity: T): Boolean {
        return entity.memoryStorage.isEmpty(CoreMemoryTypes.ENTITY_SPOUSE)
    }

    protected open fun bear(entity: T) {
        val baby: T = Entity.createEntity(entity.getNetworkID(), entity.locator) as T
        baby.setBaby(true)
        //防止小屁孩去生baby
        baby.memoryStorage.set(CoreMemoryTypes.LAST_IN_LOVE_TIME, entity.level!!.tick)
        baby.memoryStorage.set(CoreMemoryTypes.PARENT, entity)
        baby.spawnToAll()
    }
}
