package org.chorus_oss.chorus.entity.ai.executor.villager

import org.chorus_oss.chorus.block.BlockBed
import org.chorus_oss.chorus.entity.ai.executor.EntityControl
import org.chorus_oss.chorus.entity.ai.executor.IBehaviorExecutor
import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.data.EntityDataTypes
import org.chorus_oss.chorus.entity.data.EntityFlag
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.entity.mob.villagers.EntityVillagerV2
import org.chorus_oss.chorus.level.Transform
import org.chorus_oss.chorus.math.BVector3
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.BlockVector3
import org.chorus_oss.chorus.math.Vector3

class SleepExecutor : EntityControl, IBehaviorExecutor {
    override fun execute(entity: EntityMob): Boolean {
        return true
    }

    override fun onStart(entity: EntityMob) {
        removeRouteTarget(entity)
        removeLookTarget(entity)
        if (entity.memoryStorage.notEmpty(CoreMemoryTypes.OCCUPIED_BED)) {
            val bed = entity.memoryStorage.get(CoreMemoryTypes.OCCUPIED_BED)
            if (bed is BlockBed) {
                val head = bed.headPart!!
                val foot = bed.footPart!!

                val sleepingTransform = Transform(foot.locator).add(
                    when (head.blockFace) {
                        BlockFace.NORTH -> Vector3(0.5, 0.5625, 0.0)
                        BlockFace.SOUTH -> Vector3(0.5, 0.5625, 1.0)
                        BlockFace.WEST -> Vector3(0.0, 0.5625, 0.5)
                        BlockFace.EAST -> Vector3(1.0, 0.5625, 0.5)
                        else -> Vector3.ZERO
                    }
                )
                sleepingTransform.setYaw(BVector3.getYawFromVector(head.blockFace.getOpposite().unitVector))
                sleepingTransform.setHeadYaw(sleepingTransform.yaw)
                entity.teleport(sleepingTransform)
                entity.respawnToAll()
                entity.setDataFlag(EntityFlag.SLEEPING)
                entity.setDataProperty(EntityDataTypes.BED_POSITION, head.position.asBlockVector3())
                entity.setDataFlag(EntityFlag.BODY_ROTATION_BLOCKED)
            }
        }
    }

    override fun onStop(entity: EntityMob) {
        entity.setDataFlag(EntityFlag.SLEEPING, false)
        entity.setDataFlag(EntityFlag.BODY_ROTATION_BLOCKED, false)
        entity.setDataProperty(EntityDataTypes.BED_POSITION, BlockVector3(0, 0, 0))
        if (!entity.level!!.isNight) {
            if (entity is EntityVillagerV2) {
                entity.heal(entity.getMaxHealth().toFloat())
            }
        }
    }

    override fun onInterrupt(entity: EntityMob) {
        onStop(entity)
    }
}
