package org.chorus_oss.chorus.entity.mob.animal

import org.chorus_oss.chorus.entity.EntityFlyable
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.CompoundTag

class EntityParrot(chunk: IChunk?, nbt: CompoundTag) : EntityAnimal(chunk, nbt), EntityFlyable {
    override fun getEntityIdentifier(): String {
        return EntityID.PARROT
    }


    override fun getOriginalName(): String {
        return "Parrot"
    }

    override fun getWidth(): Float {
        return 0.5f
    }

    override fun getHeight(): Float {
        return 1.0f
    }

    public override fun initEntity() {
        this.maxHealth = 6
        super.initEntity()
    }

    override fun getDrops(): Array<Item> {
        return arrayOf(Item.get(ItemID.FEATHER))
    }
}
