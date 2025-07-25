package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.entity.effect.Effect
import org.chorus_oss.chorus.entity.effect.EffectType

class ItemSuspiciousStew @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    ItemFood(ItemID.Companion.SUSPICIOUS_STEW, meta, count, "Suspicious Stew") {
    override val maxStackSize: Int
        get() = 1

    override val foodRestore: Int
        get() = 6

    override val saturationRestore: Float
        get() = 7.2f

    override val isRequiresHunger: Boolean
        get() = false

    override fun onEaten(player: Player): Boolean {
        val effect = when (meta) {
            0 -> Effect.get(EffectType.NIGHT_VISION).setDuration(4 * 20)
            1 -> Effect.get(EffectType.JUMP_BOOST).setDuration(4 * 20)
            2 -> Effect.get(EffectType.WEAKNESS).setDuration(7 * 20)
            3 -> Effect.get(EffectType.BLINDNESS).setDuration(6 * 20)
            4 -> Effect.get(EffectType.POISON).setDuration(10 * 20)
            5 -> Effect.get(EffectType.SATURATION).setDuration(6 * 20)
            6 -> Effect.get(EffectType.FIRE_RESISTANCE).setDuration(2 * 20)
            7 -> Effect.get(EffectType.REGENERATION).setDuration(6 * 20)
            8 -> Effect.get(EffectType.WITHER).setDuration(6 * 20)
            else -> null
        }

        if (effect != null) {
            player.addEffect(effect)
        }

        return true
    }
}
