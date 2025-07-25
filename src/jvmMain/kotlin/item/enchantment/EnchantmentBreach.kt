package org.chorus_oss.chorus.item.enchantment

import org.chorus_oss.chorus.event.entity.EntityDamageByEntityEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent.DamageModifier

class EnchantmentBreach : Enchantment(ID_BREACH, "breach", Rarity.RARE, EnchantmentType.MACE) {
    override fun getMinEnchantAbility(level: Int): Int {
        return 10 + (level - 1) * 20
    }

    override fun getMaxEnchantAbility(level: Int): Int {
        return super.getMinEnchantAbility(level) + 50
    }

    override val maxLevel: Int
        get() = 4

    override fun checkCompatibility(enchantment: Enchantment): Boolean {
        val id = enchantment.id
        return id != ID_DAMAGE_ALL && id != ID_DENSITY && id != ID_DAMAGE_SMITE && id != ID_DAMAGE_ARTHROPODS
    }

    val armorEfficiencyReduction: Float
        get() = (100 - (this.level * 15)) / 100f

    override fun doAttack(event: EntityDamageByEntityEvent) {
        val reduction = armorEfficiencyReduction
        event.setDamage(event.getDamage(DamageModifier.ARMOR) * reduction, DamageModifier.ARMOR)
        event.setDamage(
            event.getDamage(DamageModifier.ARMOR_ENCHANTMENTS) * reduction,
            DamageModifier.ARMOR_ENCHANTMENTS
        )
    }
}
