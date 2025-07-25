package org.chorus_oss.chorus.item.enchantment

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.event.entity.EntityCombustByEntityEvent
import org.chorus_oss.chorus.event.entity.EntityDamageByEntityEvent
import kotlin.math.max


class EnchantmentFireAspect :
    Enchantment(ID_FIRE_ASPECT, "fire", Rarity.RARE, EnchantmentType.SWORD) {
    override fun getMinEnchantAbility(level: Int): Int {
        return 10 + (level - 1) * 20
    }

    override fun getMaxEnchantAbility(level: Int): Int {
        return super.getMinEnchantAbility(level) + 50
    }

    override val maxLevel: Int
        get() = 2

    override fun doAttack(event: EntityDamageByEntityEvent) {
        val entity = event.entity
        val attacker = event.damager
        if ((entity !is Player || !entity.isCreative)) {
            val duration = max((entity.fireTicks / 20).toDouble(), (this.level shl 2).toDouble()).toInt()

            val ev = EntityCombustByEntityEvent(attacker, entity, duration)
            Server.instance.pluginManager.callEvent(ev)

            if (!ev.cancelled) {
                entity.setOnFire(ev.duration)
            }
        }
    }
}
