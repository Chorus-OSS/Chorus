package org.chorus_oss.chorus.entity.mob.animal

import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.EntitySmite
import org.chorus_oss.chorus.entity.EntityWalkable
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.CompoundTag

class EntityZombieHorse(chunk: IChunk?, nbt: CompoundTag) : EntityHorse(chunk, nbt), EntityWalkable, EntitySmite {
    override fun getEntityIdentifier(): String {
        return EntityID.ZOMBIE_HORSE
    }


    override fun getWidth(): Float {
        return 1.4f
    }

    override fun getHeight(): Float {
        return 1.6f
    }

    override fun initEntity() {
        this.maxHealth = 15
        super.initEntity()
    }

    override fun getDrops(): Array<Item> {
        return arrayOf(Item.get(ItemID.ROTTEN_FLESH, 1, 1))
    }

    override fun isUndead(): Boolean {
        return true
    }

    override fun getOriginalName(): String {
        return "Zombie Horse"
    }

    override fun onUpdate(currentTick: Int): Boolean {
        burn(this)
        return super.onUpdate(currentTick)
    }
}
