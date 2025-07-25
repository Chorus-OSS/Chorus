package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.entity.effect.Effect
import org.chorus_oss.chorus.entity.effect.EffectType

class ItemPufferfish @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    ItemFish(ItemID.Companion.PUFFERFISH, meta, count) {
    override val foodRestore: Int
        get() = 1

    override val saturationRestore: Float
        get() = 0.2f

    override fun onEaten(player: Player): Boolean {
        player.addEffect(
            Effect.get(EffectType.HUNGER)
                .setDuration(15 * 20)
                .setAmplifier(2)
        )
        player.addEffect(
            Effect.get(EffectType.POISON)
                .setDuration(60 * 20)
                .setAmplifier(1)
        )
        player.addEffect(
            Effect.get(EffectType.NAUSEA)
                .setDuration(15 * 20)
                .setAmplifier(1)
        )

        return true
    }
}
