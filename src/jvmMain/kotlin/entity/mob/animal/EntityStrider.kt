package org.chorus_oss.chorus.entity.mob.animal

import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.EntityWalkable
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import java.util.concurrent.ThreadLocalRandom

class EntityStrider(chunk: IChunk?, nbt: CompoundTag) : EntityAnimal(chunk, nbt), EntityWalkable {
    override fun getEntityIdentifier(): String {
        return EntityID.STRIDER
    }

    override fun initEntity() {
        this.maxHealth = 20
        super.initEntity()
    }

    override fun getWidth(): Float {
        return 0.9f
    }

    override fun getHeight(): Float {
        return 1.7f
    }

    override fun getFrostbiteInjury(): Int {
        return 5
    }

    override fun getOriginalName(): String {
        return "Strider"
    }

    override fun getExperienceDrops(): Int {
        return ThreadLocalRandom.current().nextInt(2) + 1
    }
}
