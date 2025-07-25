package org.chorus_oss.chorus.entity.mob.monster.humanoid_monster

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.EntitySmite
import org.chorus_oss.chorus.entity.EntityWalkable
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.CompoundTag

class EntityZombieVillagerV2(chunk: IChunk?, nbt: CompoundTag?) : EntityHumanoidMonster(chunk, nbt), EntityWalkable,
    EntitySmite {
    override fun getEntityIdentifier(): String {
        return EntityID.ZOMBIE_VILLAGER_V2
    }


    override fun initEntity() {
        this.maxHealth = 20
        super.initEntity()
    }

    override fun getWidth(): Float {
        return 0.6f
    }

    override fun getHeight(): Float {
        return 1.9f
    }

    override fun getOriginalName(): String {
        return "Zombie VillagerV2"
    }

    override fun isUndead(): Boolean {
        return true
    }

    override fun isPreventingSleep(player: Player?): Boolean {
        return true
    }

    override fun onUpdate(currentTick: Int): Boolean {
        burn(this)
        return super.onUpdate(currentTick)
    }
}
