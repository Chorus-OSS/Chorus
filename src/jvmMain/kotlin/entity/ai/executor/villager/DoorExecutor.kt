package org.chorus_oss.chorus.entity.ai.executor.villager

import org.chorus_oss.chorus.block.BlockWoodenDoor
import org.chorus_oss.chorus.entity.ai.executor.EntityControl
import org.chorus_oss.chorus.entity.ai.executor.IBehaviorExecutor
import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.mob.EntityMob

class DoorExecutor : EntityControl, IBehaviorExecutor {
    override fun execute(entity: EntityMob): Boolean {
        return entity.memoryStorage.notEmpty(CoreMemoryTypes.NEAREST_DOOR) && entity.memoryStorage.get(CoreMemoryTypes.NEAREST_DOOR)!!.position.asVector3f() == entity.memoryStorage.get(
            CoreMemoryTypes.NEAREST_BLOCK_2
        )!!.position.asVector3f()
    }

    override fun onStart(entity: EntityMob) {
        if (entity.memoryStorage.notEmpty(CoreMemoryTypes.NEAREST_BLOCK_2)) {
            val block = entity.memoryStorage.get(CoreMemoryTypes.NEAREST_BLOCK_2)
            if (block is BlockWoodenDoor) {
                if (!block.isAir && !block.isOpen) block.toggle(null)
                entity.memoryStorage.set(CoreMemoryTypes.NEAREST_DOOR, block)
            }
        }
    }

    override fun onStop(entity: EntityMob) {
        if (entity.memoryStorage.notEmpty(CoreMemoryTypes.NEAREST_DOOR)) {
            val door = entity.memoryStorage.get(CoreMemoryTypes.NEAREST_DOOR)!!
            if (door.levelBlock is BlockWoodenDoor)  //Can fail when the door gets broken
                door.toggle(null)
            entity.memoryStorage.clear(CoreMemoryTypes.NEAREST_DOOR)
        }
    }

    override fun onInterrupt(entity: EntityMob) {
        onStop(entity)
    }
}
