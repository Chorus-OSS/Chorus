package org.chorus_oss.chorus.entity.mob.animal

import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.CompoundTag

class EntityTraderLlama(chunk: IChunk?, nbt: CompoundTag) : EntityLlama(chunk, nbt) {
    override fun getEntityIdentifier(): String {
        return EntityID.TRADER_LLAMA
    }

    override fun getOriginalName(): String {
        return "Trader Llama"
    }

    override fun initEntity() {
        this.maxHealth = 20
        super.initEntity()
    }
}
