package org.chorus_oss.chorus.event.entity

import com.google.common.collect.ImmutableMap
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.effect.EffectType
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.utils.EventException
import java.util.*


open class EntityDamageEvent(entity: Entity, cause: DamageCause, modifiers: Map<DamageModifier, Float>) :
    EntityEvent(), Cancellable {
    @JvmField
    val cause: DamageCause
    private val modifiers: MutableMap<DamageModifier, Float>
    private val originals: Map<DamageModifier, Float>

    var isBreakShield: Boolean = false
    var attackCooldown: Short = 10
    var shieldBreakCoolDown: Int = 100

    constructor(entity: Entity, cause: DamageCause, damage: Float) : this(
        entity,
        cause,
        createDamageModifierMap(damage)
    )

    init {
        this.entity = entity
        this.cause = cause
        this.modifiers = EnumMap(modifiers)

        this.originals = ImmutableMap.copyOf(this.modifiers)

        if (!this.modifiers.containsKey(DamageModifier.BASE)) {
            throw EventException("BASE Damage modifier missing")
        }

        if (entity.hasEffect(EffectType.RESISTANCE)) {
            this.setDamage(
                -(this.getDamage(DamageModifier.BASE) * 0.20 * entity.getEffect(EffectType.RESISTANCE)
                !!.getLevel()).toFloat(), DamageModifier.RESISTANCE
            )
        }
    }

    val originalDamage: Float
        get() = this.getOriginalDamage(DamageModifier.BASE)

    fun getOriginalDamage(type: DamageModifier): Float {
        if (originals.containsKey(type)) {
            return originals[type]!!
        }

        return 0f
    }

    var damage: Float
        get() = this.getDamage(DamageModifier.BASE)
        set(damage) {
            this.setDamage(damage, DamageModifier.BASE)
        }

    fun getDamage(type: DamageModifier): Float {
        if (modifiers.containsKey(type)) {
            return modifiers[type]!!
        }

        return 0f
    }

    fun setDamage(damage: Float, type: DamageModifier) {
        modifiers[type] = damage
    }

    fun isApplicable(type: DamageModifier): Boolean {
        return modifiers.containsKey(type)
    }

    val finalDamage: Float
        get() {
            var damage = 0f
            for (d in modifiers.values) {
                damage += d
            }

            return damage
        }

    fun canBeReducedByArmor(): Boolean {
        return when (this.cause) {
            DamageCause.FIRE_TICK, DamageCause.SUFFOCATION, DamageCause.DROWNING, DamageCause.HUNGER, DamageCause.FALL, DamageCause.VOID, DamageCause.MAGIC, DamageCause.SUICIDE -> false
            else -> true
        }
    }

    enum class DamageModifier {
        /**
         * Raw amount of damage
         */
        BASE,

        /**
         * Damage reduction caused by wearing armor
         */
        ARMOR,

        /**
         * Additional damage caused by damager's Strength potion effect
         */
        STRENGTH,

        /**
         * Damage reduction caused by damager's Weakness potion effect
         */
        WEAKNESS,

        /**
         * Damage reduction caused by the Resistance potion effect
         */
        RESISTANCE,

        /**
         * Damage reduction caused by the Damage absorption effect
         */
        ABSORPTION,

        /**
         * Damage reduction caused by the armor enchantments worn.
         */
        ARMOR_ENCHANTMENTS
    }

    enum class DamageCause {
        /**
         * Damage caused by contact with a block such as a Cactus
         */
        CONTACT,

        /**
         * Damage caused by being attacked by another entity
         */
        ENTITY_ATTACK,

        /**
         * Damage caused by being hit by a projectile such as an Arrow
         */
        PROJECTILE,

        /**
         * Damage caused by being put in a block
         */
        SUFFOCATION,

        /**
         * Fall damage
         */
        FALL,

        /**
         * Damage caused by standing in fire
         */
        FIRE,

        /**
         * Burn damage
         */
        FIRE_TICK,

        /**
         * Damage caused by standing in lava
         */
        LAVA,

        /**
         * Damage caused by running out of air underwater
         */
        DROWNING,

        /**
         * Block explosion damage
         */
        BLOCK_EXPLOSION,

        /**
         * Entity explosion damage
         */
        ENTITY_EXPLOSION,

        /**
         * Damage caused by falling into the void
         */
        VOID,

        /**
         * Player commits suicide
         */
        SUICIDE,

        /**
         * Potion or spell damage
         */
        MAGIC,

        /**
         * Plugins
         */
        CUSTOM,

        /**
         * Damage caused by being struck by lightning
         */
        LIGHTNING,

        /**
         * Damage caused by hunger
         */
        HUNGER,

        /**
         * Damage caused by Wither
         */
        WITHER,

        /**
         * Damage caused by weather like a snowman takes damage by rain
         */
        WEATHER,

        /**
         * Damage caused by thorns
         */
        THORNS,

        /**
         * Damage caused by falling block
         */
        FALLING_BLOCK,

        /**
         * Damage caused by flying into wall
         */
        FLYING_INTO_WALL,

        /**
         * Damage caused when an entity steps on a hot block, like [org.chorus_oss.chorus.block.BlockID.MAGMA]
         */
        HOT_FLOOR,

        /**
         * Damage caused by fireworks
         */
        FIREWORKS,

        /**
         * Damage caused by temperature
         */
        FREEZING,

        /**
         * Damage caused by no reason (eg: /damage command with cause NONE)
         */
        NONE,

        /**
         * Damage caused by a lot of (>24) entities colliding together
         */
        COLLIDE,

        /**
         * Damage caused by mace attack
         */
        MACE_SMASH,

        /**
         * Damage caused by ageing
         */
        AGE
    }

    companion object {
        val handlers: HandlerList = HandlerList()

        private fun createDamageModifierMap(baseDamage: Float): Map<DamageModifier, Float> {
            val modifiers: MutableMap<DamageModifier, Float> = EnumMap(
                DamageModifier::class.java
            )
            modifiers[DamageModifier.BASE] = baseDamage
            return modifiers
        }
    }
}
