package org.chorus_oss.chorus.entity.projectile

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.effect.PotionType
import org.chorus_oss.chorus.entity.item.EntityAreaEffectCloud
import org.chorus_oss.chorus.entity.mob.monster.EntityEnderDragon
import org.chorus_oss.chorus.level.Locator
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.FloatTag
import org.chorus_oss.chorus.nbt.tag.ListTag

class EntityDragonFireball(chunk: IChunk?, nbt: CompoundTag?) : EntityProjectile(chunk, nbt) {
    override fun getEntityIdentifier(): String {
        return EntityID.DRAGON_FIREBALL
    }

    override fun getHeight(): Float {
        return 0.3125f
    }

    override fun getWidth(): Float {
        return 0.3125f
    }

    override fun getLength(): Float {
        return 0.3125f
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

    override fun onCollideWithEntity(entity: Entity) {
        if (entity !is EntityEnderDragon) onCollide()
    }

    override fun onCollideWithBlock(locator: Locator, motion: Vector3) {
        onCollide()
    }

    protected fun onCollide() {
        this.close()
        val entity = createEntity(
            EntityID.AREA_EFFECT_CLOUD,
            locator.chunk,
            CompoundTag().putList(
                "Pos", ListTag<FloatTag>()
                    .add(FloatTag(position.x))
                    .add(FloatTag(position.y))
                    .add(FloatTag(position.z))
            )
                .putList(
                    "Rotation", ListTag<FloatTag>()
                        .add(FloatTag(0f))
                        .add(FloatTag(0f))
                )
                .putList(
                    "Motion", ListTag<FloatTag>()
                        .add(FloatTag(0f))
                        .add(FloatTag(0f))
                        .add(FloatTag(0f))
                )
                .putInt("Duration", 2400)
                .putFloat("InitialRadius", 6f)
                .putFloat("Radius", 6f)
                .putFloat("RadiusChangeOnPickup", 0f)
                .putFloat("RadiusPerTick", 0f)
        ) as EntityAreaEffectCloud?

        val effects = PotionType.get(PotionType.HARMING_STRONG.id).getEffects(false)
        for (effect in effects) {
            if (entity != null) {
                entity.cloudEffects!!.add(effect.setVisible(false).setAmbient(false))
                entity.spawnToAll()
            }
        }
        entity!!.spawnToAll()
    }

    override fun getOriginalName(): String {
        return "Dragon FireBall"
    }
}
