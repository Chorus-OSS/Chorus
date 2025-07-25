package org.chorus_oss.chorus.entity.mob.animal

import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.CompoundTag

open class EntityLlama(chunk: IChunk?, nbt: CompoundTag) : EntityAnimal(chunk, nbt) {
    override fun getEntityIdentifier(): String {
        return EntityID.LLAMA
    }

    override fun getWidth(): Float {
        return 0.6f
    }

    override fun getHeight(): Float {
        return 1.9f
    }

    override fun getOriginalName(): String {
        return "Llama"
    }

    public override fun initEntity() {
        this.maxHealth = 20
        super.initEntity()
    }
}
