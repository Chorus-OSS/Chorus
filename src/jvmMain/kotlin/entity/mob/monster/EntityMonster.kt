package org.chorus_oss.chorus.entity.mob.monster

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.entity.EntityCanAttack
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.item.enchantment.Enchantment
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import kotlin.math.max

abstract class EntityMonster(chunk: IChunk?, nbt: CompoundTag) : EntityMob(chunk, nbt), EntityCanAttack {
    private var spawnedByNight: Boolean = true

    override fun initEntity() {
        super.initEntity()

        spawnedByNight = namedTag!!.getBoolean(TAG_SPAWNED_BY_NIGHT)
    }

    override fun onUpdate(currentTick: Int): Boolean {
        if (Server.instance.getDifficulty() == 0) {
            this.close()
            return true
        } else return super.onUpdate(currentTick)
    }

    override fun saveNBT() {
        super.saveNBT()

        namedTag!!.putBoolean(TAG_SPAWNED_BY_NIGHT, spawnedByNight)
    }

    override fun setOnFire(seconds: Int) {
        var seconds1 = seconds
        var level = 0

        for (armor in equipment.getArmor()) {
            val fireProtection = armor.getEnchantment(Enchantment.ID_PROTECTION_FIRE)

            if (fireProtection != null && fireProtection.level > 0) {
                level = max(level.toDouble(), fireProtection.level.toDouble()).toInt()
            }
        }

        seconds1 = (seconds1 * (1 - level * 0.15)).toInt()

        super.setOnFire(seconds1)
    }

    companion object {
        private const val TAG_SPAWNED_BY_NIGHT = "SpawnedByNight"
    }
}
