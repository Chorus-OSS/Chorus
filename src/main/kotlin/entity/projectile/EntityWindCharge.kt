package org.chorus_oss.chorus.entity.projectile

import org.chorus_oss.chorus.block.*
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.EntityLiving
import org.chorus_oss.chorus.event.entity.EntityDamageByEntityEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent.DamageCause
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.level.Locator
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.level.particle.GenericParticle
import org.chorus_oss.chorus.level.particle.Particle
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.nbt.tag.CompoundTag

open class EntityWindCharge @JvmOverloads constructor(
    chunk: IChunk?,
    nbt: CompoundTag?,
    shootingEntity: Entity? = null
) :
    EntityProjectile(chunk, nbt, shootingEntity) {
    var directionChanged: Entity? = null

    override fun getEntityIdentifier(): String {
        return EntityID.WIND_CHARGE_PROJECTILE
    }

    override fun onCollideWithBlock(locator: Locator, motion: Vector3, collisionBlock: Block): Boolean {
        if (collisionBlock is BlockDoor
            || collisionBlock is BlockTrapdoor
            || collisionBlock is BlockFenceGate
            || collisionBlock is BlockButton
            || collisionBlock is BlockLever
            || collisionBlock is BlockCandle
            || collisionBlock is BlockCandleCake
        ) {
            collisionBlock.onActivate(Item.AIR, null, getDirection(), 0f, 0f, 0f)
        }

        if (collisionBlock is BlockChorusFlower
            || collisionBlock is BlockDecoratedPot
        ) {
            level!!.useBreakOn(collisionBlock.position, Item.AIR)
        }

        for (entity: Entity in level!!.getEntities()) {
            if (entity is EntityLiving) {
                if (entity.position.distance(this.position) < getBurstRadius()) {
                    this.knockBack(entity)
                }
            }
        }
        level!!.addLevelSoundEvent(
            locator.position.add(0.0, 1.0),
            org.chorus_oss.protocol.packets.LevelSoundEventPacket.Companion.SoundType.WindChargeBurst
        )
        this.kill()

        return true
    }

    override fun onCollideWithEntity(entity: Entity) {
        if (directionChanged != null) {
            if (directionChanged === entity) return
        }
        entity.attack(EntityDamageByEntityEvent(this, entity, DamageCause.PROJECTILE, 1f))
        level!!.addLevelSoundEvent(
            entity.locator.position.add(0.0, 1.0),
            org.chorus_oss.protocol.packets.LevelSoundEventPacket.Companion.SoundType.WindChargeBurst
        )
        level!!.addParticle(GenericParticle(this.position, Particle.TYPE_WIND_EXPLOSION))
        knockBack(entity)
        this.kill()
    }

    override fun attack(source: EntityDamageEvent): Boolean {
        if (this.directionChanged == null && source is EntityDamageByEntityEvent) {
            this.directionChanged = source.damager
            this.setMotion(source.damager.getDirectionVector())
        }
        return true
    }

    override fun addHitEffect() {
        level!!.addParticle(GenericParticle(this.position, Particle.TYPE_WIND_EXPLOSION))
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

    override fun getWidth(): Float {
        return 0.3125f
    }

    override fun getLength(): Float {
        return 0.3125f
    }

    override fun getHeight(): Float {
        return 0.3125f
    }

    override fun getGravity(): Float {
        return 0.00f
    }

    override fun getDrag(): Float {
        return 0.01f
    }

    override fun getOriginalName(): String {
        return "Wind Charge Projectile"
    }

    open fun getBurstRadius(): Double {
        return 2.0
    }

    open fun getKnockbackStrength(): Double {
        return 0.2
    }

    protected fun knockBack(entity: Entity) {
        val knockback: Vector3 = Vector3(entity.motion.x, entity.motion.y, entity.motion.z)
        knockback.x /= 2.0
        knockback.y /= 2.0
        knockback.z /= 2.0
        knockback.x -= (this.getX() - entity.getX()) * getKnockbackStrength()
        knockback.y += 0.3
        knockback.z -= (this.getZ() - entity.getZ()) * getKnockbackStrength()

        entity.setMotion(knockback)
    }
}
