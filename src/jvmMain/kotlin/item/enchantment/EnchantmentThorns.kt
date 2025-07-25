package org.chorus_oss.chorus.item.enchantment

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityHumanType
import org.chorus_oss.chorus.event.entity.EntityDamageByEntityEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent.DamageCause
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemElytra
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.max


class EnchantmentThorns :
    Enchantment(ID_THORNS, "thorns", Rarity.VERY_RARE, EnchantmentType.ARMOR) {
    override fun getMinEnchantAbility(level: Int): Int {
        return 10 + (level - 1) * 20
    }

    override fun getMaxEnchantAbility(level: Int): Int {
        return super.getMinEnchantAbility(level) + 50
    }

    override val maxLevel: Int
        get() = 3

    override fun doPostAttack(attacker: Entity, entity: Entity) {
        if (entity !is EntityHumanType) {
            return
        }

        var thornsLevel = 0

        for (armor in entity.inventory.armorContents) {
            val thorns = armor.getEnchantment(ID_THORNS)
            if (thorns != null) {
                thornsLevel = max(thorns.level.toDouble(), thornsLevel.toDouble()).toInt()
            }
        }

        val random = ThreadLocalRandom.current()

        if (shouldHit(random, thornsLevel)) {
            attacker.attack(
                EntityDamageByEntityEvent(
                    entity,
                    attacker,
                    DamageCause.THORNS,
                    getDamage(random, level).toFloat(),
                    0f
                )
            )
        }
    }

    override fun canEnchant(item: Item): Boolean {
        return item !is ItemElytra && super.canEnchant(item)
    }

    companion object {
        private fun shouldHit(random: ThreadLocalRandom, level: Int): Boolean {
            return level > 0 && random.nextFloat() < 0.15 * level
        }

        private fun getDamage(random: ThreadLocalRandom, level: Int): Int {
            return if (level > 10) level - 10 else random.nextInt(1, 5)
        }
    }
}
