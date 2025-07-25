package org.chorus_oss.chorus.entity.ai.executor

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.entity.ai.evaluator.EntityCheckEvaluator
import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.data.EntityFlag
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.level.Sound

class StaringAttackTargetExecutor : IBehaviorExecutor {
    override fun execute(entity: EntityMob): Boolean {
        if (entity.memoryStorage.isEmpty(CoreMemoryTypes.ATTACK_TARGET)) {
            if (!entity.memoryStorage.isEmpty(CoreMemoryTypes.STARING_PLAYER) && EntityCheckEvaluator(
                    CoreMemoryTypes.STARING_PLAYER
                ).evaluate(entity)
            ) {
                entity.memoryStorage.set(
                    CoreMemoryTypes.ATTACK_TARGET, entity.memoryStorage
                        .get(CoreMemoryTypes.STARING_PLAYER)
                )
                entity.level!!.addSound(entity.position, Sound.MOB_ENDERMEN_STARE)
            } else if (!entity.memoryStorage.isEmpty(CoreMemoryTypes.NEAREST_ENDERMITE)) {
                entity.memoryStorage.set(
                    CoreMemoryTypes.ATTACK_TARGET, entity.memoryStorage
                        .get(CoreMemoryTypes.NEAREST_ENDERMITE)
                )
            }
        } else {
            val player = entity.memoryStorage.get(CoreMemoryTypes.ATTACK_TARGET)
            if (player is Player) {
                if (!player.isOnline) {
                    entity.memoryStorage.clear(CoreMemoryTypes.STARING_PLAYER)
                    entity.memoryStorage.clear(CoreMemoryTypes.ATTACK_TARGET)
                }
            }
        }
        if (entity.memoryStorage.isEmpty(CoreMemoryTypes.ATTACK_TARGET)) {
            if (entity.getDataFlag(EntityFlag.ANGRY)) {
                entity.setDataFlag(EntityFlag.ANGRY, false)
            }
        } else {
            if (EntityCheckEvaluator(CoreMemoryTypes.ATTACK_TARGET).evaluate(entity)) {
                if (!entity.getDataFlag(EntityFlag.ANGRY)) {
                    entity.setDataFlag(EntityFlag.ANGRY)
                }
            } else entity.memoryStorage.clear(CoreMemoryTypes.ATTACK_TARGET)
        }
        return true
    }
}
