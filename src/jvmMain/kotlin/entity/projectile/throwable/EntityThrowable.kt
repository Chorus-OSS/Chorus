package org.chorus_oss.chorus.entity.projectile.throwable

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.projectile.EntityProjectile
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.CompoundTag

abstract class EntityThrowable @JvmOverloads constructor(
    chunk: IChunk?,
    nbt: CompoundTag,
    shootingEntity: Entity? = null
) :
    EntityProjectile(chunk, nbt, shootingEntity) {
    var inGround: Boolean = false
    var ownerId: Long = -1L
    var shake: Boolean = false

    init {
        this.inGround = nbt.getBoolean(TAG_IN_GROUND)
        this.ownerId = nbt.getLong(TAG_OWNER_ID)
        this.shake = nbt.getBoolean(TAG_SHAKE)
    }

    companion object {
        const val TAG_IN_GROUND: String = "InGround"
        const val TAG_OWNER_ID: String = "OwnerID"
        const val TAG_SHAKE: String = "shake"
    }
}
