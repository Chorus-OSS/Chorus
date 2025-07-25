package org.chorus_oss.chorus.entity.projectile

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityExplosive
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.effect.Effect
import org.chorus_oss.chorus.entity.effect.EffectType
import org.chorus_oss.chorus.entity.mob.monster.EntityWither
import org.chorus_oss.chorus.event.entity.EntityDamageEvent
import org.chorus_oss.chorus.event.entity.EntityExplosionPrimeEvent
import org.chorus_oss.chorus.level.Explosion
import org.chorus_oss.chorus.level.GameRule
import org.chorus_oss.chorus.level.Locator
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.level.vibration.VibrationEvent
import org.chorus_oss.chorus.level.vibration.VibrationType
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.nbt.tag.CompoundTag

open class EntityWitherSkull(chunk: IChunk?, nbt: CompoundTag?) : EntityProjectile(chunk, nbt), EntityExplosive {
    override fun getEntityIdentifier(): String {
        return EntityID.WITHER_SKULL
    }

    override fun getDamage(): Double {
        return when (Server.instance.getDifficulty()) {
            1 -> 5.0
            2 -> 8.0
            3 -> 12.0
            else -> 0.0
        }
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

    protected open fun getStrength(): Float {
        return 1.2f
    }

    override fun onCollideWithEntity(entity: Entity) {
        if (entity !is EntityWither) {
            explode()
            entity.addEffect(Effect.get(EffectType.WITHER).setDuration(200))
            super.onCollideWithEntity(entity)
        }
    }

    override fun onCollideWithBlock(locator: Locator, motion: Vector3) {
        level!!.vibrationManager.callVibrationEvent(
            VibrationEvent(
                this,
                this.vector3, VibrationType.PROJECTILE_LAND
            )
        )
        var affect: Boolean = false
        for (collisionBlock: Block in level!!.getCollisionBlocks(getBoundingBox().grow(0.1, 0.1, 0.1))) affect =
            onCollideWithBlock(locator, motion, collisionBlock)
        if (!affect && locator.levelBlock.isAir) {
            explode()
        }
    }

    override fun attack(source: EntityDamageEvent): Boolean {
        return false
    }

    override fun getOriginalName(): String {
        return "Wither Skull"
    }

    override fun explode() {
        if (this.closed) {
            return
        }
        this.close()
        val ev: EntityExplosionPrimeEvent = EntityExplosionPrimeEvent(this, getStrength().toDouble())
        ev.fireChance = 0.0
        Server.instance.pluginManager.callEvent(ev)
        if (!ev.cancelled) {
            val explosion = Explosion(this.locator, ev.force.toFloat().toDouble(), this.shootingEntity!!)
            explosion.fireChance = ev.fireChance
            if (ev.isBlockBreaking && level!!.gameRules.getBoolean(GameRule.MOB_GRIEFING)) {
                explosion.explodeA()
            }
            explosion.explodeB()
        }
    }
}
