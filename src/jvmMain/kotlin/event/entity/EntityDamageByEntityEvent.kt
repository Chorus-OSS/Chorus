package org.chorus_oss.chorus.event.entity

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.effect.EffectType
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.item.enchantment.Enchantment


open class EntityDamageByEntityEvent : EntityDamageEvent {
    @JvmField
    val damager: Entity

    var knockBack: Float
    private var enchantments: Array<Enchantment>? = null

    @JvmOverloads
    constructor(damager: Entity, entity: Entity, cause: DamageCause, damage: Float, knockBack: Float = 0.3f) : super(
        entity,
        cause,
        damage
    ) {
        this.damager = damager
        this.knockBack = knockBack
        this.addAttackerModifiers(damager)
    }

    @JvmOverloads
    constructor(
        damager: Entity,
        entity: Entity,
        cause: DamageCause,
        modifiers: Map<DamageModifier, Float>,
        knockBack: Float = 0.3f,
        enchantments: Array<Enchantment>? = null
    ) : super(entity, cause, modifiers) {
        this.damager = damager
        this.knockBack = knockBack
        this.enchantments = enchantments
        this.addAttackerModifiers(damager)
    }

    protected fun addAttackerModifiers(damager: Entity) {
        if (damager.hasEffect(EffectType.STRENGTH)) {
            this.setDamage(
                (this.getDamage(DamageModifier.BASE) * 0.3 * damager.getEffect(EffectType.STRENGTH)!!
                    .getLevel()).toFloat(), DamageModifier.STRENGTH
            )
        }

        if (damager.hasEffect(EffectType.WEAKNESS)) {
            this.setDamage(
                -(this.getDamage(DamageModifier.BASE) * 0.2 * damager.getEffect(EffectType.WEAKNESS)!!
                    .getLevel()).toFloat(), DamageModifier.WEAKNESS
            )
        }
    }

    val weaponEnchantments: Array<Enchantment>?
        get() {
            if (enchantments == null) {
                return null
            }
            return if (enchantments!!.isNotEmpty()) enchantments!!.clone() else Enchantment.EMPTY_ARRAY
        }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
