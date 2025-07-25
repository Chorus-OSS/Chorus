package org.chorus_oss.chorus.entity.projectile.throwable

import org.chorus_oss.chorus.entity.ClimateVariant
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.item.ItemEgg
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.level.particle.ItemBreakParticle
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import java.util.concurrent.ThreadLocalRandom


class EntityEgg @JvmOverloads constructor(chunk: IChunk?, nbt: CompoundTag, shootingEntity: Entity? = null) :
    EntityThrowable(chunk, nbt, shootingEntity), ClimateVariant {
    override fun getEntityIdentifier(): String {
        return EntityID.EGG
    }

    override fun getWidth(): Float {
        return 0.25f
    }

    override fun getLength(): Float {
        return 0.25f
    }

    override fun getHeight(): Float {
        return 0.25f
    }

    override fun getGravity(): Float {
        return 0.03f
    }

    override fun getDrag(): Float {
        return 0.01f
    }

    override fun onUpdate(currentTick: Int): Boolean {
        if (this.closed) {
            return false
        }

        var hasUpdate: Boolean = super.onUpdate(currentTick)

        if (this.age > 1200 || this.isCollided) {
            this.kill()
            hasUpdate = true
        }

        return hasUpdate
    }

    override fun addHitEffect() {
        val particles: Int = ThreadLocalRandom.current().nextInt(10) + 5
        val egg: ItemEgg = ItemEgg()
        for (i in 0..<particles) {
            level!!.addParticle(ItemBreakParticle(this.position, egg))
        }
    }

    override fun getOriginalName(): String {
        return "Egg"
    }

    override fun initEntity() {
        super.initEntity()
        if (namedTag!!.contains("variant")) {
            this.climateVariant = ClimateVariant.Companion.Variant.get(namedTag!!.getString("variant"))
        } else {
            this.climateVariant = ClimateVariant.Companion.Variant.Temperate
        }
    }
}
