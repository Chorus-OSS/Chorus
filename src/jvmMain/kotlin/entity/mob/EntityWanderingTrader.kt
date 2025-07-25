package org.chorus_oss.chorus.entity.mob

import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.CompoundTag

class EntityWanderingTrader(chunk: IChunk?, nbt: CompoundTag) : EntityMob(chunk, nbt) {
    override fun getEntityIdentifier(): String {
        return EntityID.WANDERING_TRADER
    }

    override fun getWidth(): Float {
        return 0.6f
    }

    override fun getHeight(): Float {
        return 1.9f
    }

    override fun getOriginalName(): String {
        return "Wandering Trader"
    }

    public override fun initEntity() {
        this.setMaxHealth(20)
        super.initEntity()
    }

    override fun getExperienceDrops(): Int {
        return 0
    }
}
