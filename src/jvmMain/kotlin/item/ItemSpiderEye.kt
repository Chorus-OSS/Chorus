package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.entity.effect.Effect
import org.chorus_oss.chorus.entity.effect.EffectType

class ItemSpiderEye @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    ItemFood(ItemID.Companion.SPIDER_EYE, meta, count, "Spider Eye") {
    override val foodRestore: Int
        get() = 2

    override val saturationRestore: Float
        get() = 3.2f

    override fun onEaten(player: Player): Boolean {
        player.addEffect(Effect.get(EffectType.POISON).setDuration(5 * 20))

        return true
    }
}
