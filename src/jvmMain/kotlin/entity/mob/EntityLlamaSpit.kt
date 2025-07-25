package org.chorus_oss.chorus.entity.mob

import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.EntityWalkable
import org.chorus_oss.chorus.entity.mob.animal.EntityAnimal
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.CompoundTag

class EntityLlamaSpit(chunk: IChunk?, nbt: CompoundTag) : EntityAnimal(chunk, nbt), EntityWalkable {
    override fun getEntityIdentifier(): String {
        return EntityID.LLAMA_SPIT
    }

    override fun getWidth(): Float {
        if (this.isBaby()) {
            return 0.45f
        }
        return 0.9f
    }

    override fun getHeight(): Float {
        if (this.isBaby()) {
            return 0.935f
        }
        return 1.87f
    }

    override fun getEyeHeight(): Float {
        if (this.isBaby()) {
            return 0.65f
        }
        return 1.2f
    }

    public override fun initEntity() {
        this.setMaxHealth(15)
        super.initEntity()
    }

    override fun getOriginalName(): String {
        return "Llama"
    }
}
