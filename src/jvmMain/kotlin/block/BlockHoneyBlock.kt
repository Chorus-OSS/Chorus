package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.AdventureSettings
import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.effect.EffectType
import org.chorus_oss.chorus.event.entity.EntityDamageEvent
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.math.AxisAlignedBB
import org.chorus_oss.chorus.math.SimpleAxisAlignedBB
import java.util.*
import kotlin.math.abs
import kotlin.math.floor

class BlockHoneyBlock @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockSolid(blockstate) {
    override val name: String
        get() = "Honey Block"

    override val hardness: Double
        get() = 0.0

    override val resistance: Double
        get() = 0.0

    override fun onEntityCollide(entity: Entity) {
        if (!entity.onGround && entity.motion.y <= 0.08 &&
            (entity !is Player || !entity.adventureSettings.get(AdventureSettings.Type.FLYING))
        ) {
            val ex = abs(position.x + 0.5 - entity.position.x)
            val ez = abs(position.z + 0.5 - entity.position.z)
            val width = 0.4375 + (entity.getWidth() / 2.0f).toDouble()
            if (ex + 1.0E-3 > width || ez + 1.0E-3 > width) {
                val motion = entity.getMotion()
                motion.y = -0.05
                if (entity.motion.y < -0.13) {
                    val m = -0.05 / entity.motion.y
                    motion.x *= m
                    motion.z *= m
                }

                if (!entity.getMotion().equals(motion)) {
                    entity.setMotion(motion)
                }
                entity.resetFallDistance()

                if (RANDOM.nextInt(10) == 0) {
                    level.addSound(entity.position, Sound.LAND_HONEY_BLOCK)
                }
            }
        }
    }

    override fun recalculateCollisionBoundingBox(): AxisAlignedBB {
        return SimpleAxisAlignedBB(
            position.x,
            position.y,
            position.z, position.x + 1, position.y + 1, position.z + 1
        )
    }

    override var minX: Double
        get() = position.x + 0.1
        set(minX) {
            super.minX = minX
        }

    override var maxX: Double
        get() = position.x + 0.9
        set(maxX) {
            super.maxX = maxX
        }

    override var minZ: Double
        get() = position.z + 0.1
        set(minZ) {
            super.minZ = minZ
        }

    override var maxZ: Double
        get() = position.z + 0.9
        set(maxZ) {
            super.maxZ = maxZ
        }

    override val lightFilter: Int
        get() = 1

    override fun useDefaultFallDamage(): Boolean {
        return false
    }

    override fun onEntityFallOn(entity: Entity, fallDistance: Float) {
        val jumpBoost = entity.getEffect(EffectType.JUMP_BOOST)?.getLevel() ?: 0
        var damage = floor((fallDistance - 3 - jumpBoost).toDouble()).toFloat()

        damage *= 0.2f

        if (damage > 0) {
            entity.attack(EntityDamageEvent(entity, EntityDamageEvent.DamageCause.FALL, damage))
        }
    }

    override fun canSticksBlock(): Boolean {
        return true
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        private val RANDOM = Random()

        val properties: BlockProperties = BlockProperties(BlockID.HONEY_BLOCK)
    }
}