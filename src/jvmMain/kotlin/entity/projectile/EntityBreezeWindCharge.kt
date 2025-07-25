package org.chorus_oss.chorus.entity.projectile

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.CompoundTag

class EntityBreezeWindCharge @JvmOverloads constructor(
    chunk: IChunk?,
    nbt: CompoundTag?,
    shootingEntity: Entity? = null
) :
    EntityWindCharge(chunk, nbt, shootingEntity) {
    override fun getEntityIdentifier(): String {
        return EntityID.BREEZE_WIND_CHARGE_PROJECTILE
    }

    override fun getOriginalName(): String {
        return "Breeze Wind Charge Projectile"
    }

    override fun getBurstRadius(): Double {
        return 3.0
    }

    override fun getKnockbackStrength(): Double {
        return 0.18
    }
}
