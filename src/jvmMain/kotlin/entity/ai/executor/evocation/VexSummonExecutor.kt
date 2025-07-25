package org.chorus_oss.chorus.entity.ai.executor.evocation

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.data.EntityDataTypes
import org.chorus_oss.chorus.entity.data.EntityFlag
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.entity.mob.monster.EntityVex
import org.chorus_oss.chorus.entity.mob.monster.humanoid_monster.EntityEvocationIllager
import org.chorus_oss.chorus.entity.mob.monster.humanoid_monster.EntityEvocationIllager.Spell
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.FloatTag
import org.chorus_oss.chorus.nbt.tag.ListTag
import org.chorus_oss.chorus.utils.BlockColor
import java.util.concurrent.ThreadLocalRandom

class VexSummonExecutor : FangLineExecutor() {
    override fun execute(entity: EntityMob): Boolean {
        tick++
        if (tick == CAST_DURATION) {
            for (i in 0..<VEX_COUNT) {
                var count = 0
                for (entity1 in entity.level!!.getNearbyEntities(
                    entity.getBoundingBox().grow(15.0, 15.0, 15.0)
                )) {
                    if (entity1 is EntityVex) count++
                }
                if (count < 8) {
                    summon(entity)
                }
            }
        }
        if (tick >= CAST_DURATION) {
            val tick = entity.level!!.tick
            entity.memoryStorage.set(CoreMemoryTypes.LAST_ATTACK_SUMMON, tick)
            entity.memoryStorage.set(CoreMemoryTypes.LAST_ATTACK_TIME, tick)
            return false
        } else return true
    }

    override fun startSpell(entity: EntityMob) {
        tick = 0
        entity.level!!.addSound(entity.position, Sound.MOB_EVOCATION_ILLAGER_PREPARE_SUMMON)
        entity.setDataProperty(EntityDataTypes.EVOKER_SPELL_CASTING_COLOR, BlockColor.WHITE_BLOCK_COLOR.argb)
        entity.memoryStorage.set(CoreMemoryTypes.LAST_MAGIC, Spell.SUMMON)
        entity.setDataFlag(EntityFlag.CASTING)
    }

    protected fun summon(entity: EntityMob) {
        if (!entity.getDataFlag(EntityFlag.CASTING)) return
        val vexTransform = entity.transform
        vexTransform.position.x += ThreadLocalRandom.current().nextFloat(2f).toDouble()
        vexTransform.position.y += ThreadLocalRandom.current().nextFloat(2f).toDouble()
        vexTransform.position.z += ThreadLocalRandom.current().nextFloat(2f).toDouble()
        val nbt = CompoundTag()
            .putList(
                "Pos", ListTag<FloatTag>()
                    .add(FloatTag(vexTransform.position.x))
                    .add(FloatTag(vexTransform.position.y))
                    .add(FloatTag(vexTransform.position.z))
            )
            .putList(
                "Motion", ListTag<FloatTag>()
                    .add(FloatTag(0f))
                    .add(FloatTag(0f))
                    .add(FloatTag(0f))
            )
            .putList(
                "Rotation", ListTag<FloatTag>()
                    .add(FloatTag((if (entity.headYaw > 180) 360 else 0).toFloat() - entity.headYaw))
                    .add(FloatTag(0f))
            )

        val vexEntity = Entity.Companion.createEntity(
            EntityID.VEX,
            entity.level!!.getChunk(entity.position.chunkX, entity.position.chunkZ),
            nbt
        )
        if (vexEntity is EntityVex) {
            vexEntity.illager = entity as EntityEvocationIllager
        }
        vexEntity?.spawnToAll()
    }

    companion object {
        //Values represent ticks
        private const val CAST_DURATION = 100
        private const val VEX_COUNT = 3
    }
}
