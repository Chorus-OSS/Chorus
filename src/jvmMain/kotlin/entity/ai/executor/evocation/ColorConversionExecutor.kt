package org.chorus_oss.chorus.entity.ai.executor.evocation

import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.data.EntityDataTypes
import org.chorus_oss.chorus.entity.data.EntityFlag
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.entity.mob.animal.EntitySheep
import org.chorus_oss.chorus.entity.mob.monster.humanoid_monster.EntityEvocationIllager.Spell
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.utils.BlockColor
import org.chorus_oss.chorus.utils.DyeColor

class ColorConversionExecutor : FangLineExecutor() {
    override fun execute(entity: EntityMob): Boolean {
        tick++
        if (tick == CAST_DURATION) {
            for (entity1 in entity.level!!.getNearbyEntities(
                entity.getBoundingBox().grow(16.0, 16.0, 16.0)
            )) {
                if (entity1 is EntitySheep) {
                    if (entity1.getColor() == DyeColor.BLUE.woolData) {
                        entity1.setColor(DyeColor.RED.woolData)
                    }
                }
            }
        }
        if (tick >= CAST_DURATION) {
            val tick = entity.level!!.tick
            entity.memoryStorage.set(CoreMemoryTypes.LAST_CONVERSION, tick)
            entity.memoryStorage.set(CoreMemoryTypes.LAST_ATTACK_TIME, tick)
            return false
        } else return true
    }

    override fun startSpell(entity: EntityMob) {
        tick = 0
        entity.level!!.addSound(entity.position, Sound.MOB_EVOCATION_ILLAGER_PREPARE_WOLOLO)
        entity.setDataProperty(EntityDataTypes.EVOKER_SPELL_CASTING_COLOR, BlockColor.ORANGE_BLOCK_COLOR.argb)
        entity.memoryStorage.set(CoreMemoryTypes.LAST_MAGIC, Spell.COLOR_CONVERSION)
        entity.setDataFlag(EntityFlag.CASTING)
    }

    companion object {
        //Values represent ticks
        private const val CAST_DURATION = 60
    }
}
