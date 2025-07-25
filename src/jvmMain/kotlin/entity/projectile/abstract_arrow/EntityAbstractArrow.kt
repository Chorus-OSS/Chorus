package org.chorus_oss.chorus.entity.projectile.abstract_arrow

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.projectile.SlenderProjectile
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.CompoundTag

abstract class EntityAbstractArrow @JvmOverloads constructor(
    chunk: IChunk?,
    nbt: CompoundTag,
    shootingEntity: Entity? = null
) :
    SlenderProjectile(chunk, nbt, shootingEntity) {
    open val isCreative: Boolean = nbt.getBoolean(TAG_IS_CREATIVE)
    var OwnerID: Long = -1L
    var player: Boolean = false

    init {
        this.OwnerID = nbt.getLong(TAG_OWNER_ID)
        this.player = nbt.getBoolean(TAG_PLAYER)
    }

    companion object {
        const val TAG_IS_CREATIVE: String = "isCreative"
        const val TAG_OWNER_ID: String = "OwnerID"
        const val TAG_PLAYER: String = "player"
    }
}
