package org.chorus_oss.chorus.entity.projectile

import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.CompoundTag

class EntityWitherSkullDangerous(chunk: IChunk?, nbt: CompoundTag?) : EntityWitherSkull(chunk, nbt) {
    override fun getEntityIdentifier(): String {
        return EntityID.WITHER_SKULL_DANGEROUS
    }

    override fun getStrength(): Float {
        return 1.5f
    }
}
