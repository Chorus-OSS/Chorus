package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.BlockPotatoes
import org.chorus_oss.chorus.entity.effect.Effect
import org.chorus_oss.chorus.entity.effect.EffectType

class ItemPoisonousPotato @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    ItemPotato(ItemID.POISONOUS_POTATO, meta, count, "Poisonous Potato") {
    init {
        this.blockState = BlockPotatoes.properties.defaultState
    }

    override fun onEaten(player: Player): Boolean {
        if (0.6f >= Math.random()) {
            player.addEffect(Effect.get(EffectType.POISON).setDuration(80))
        }
        return true
    }

    override val foodRestore: Int
        get() = 2

    override val saturationRestore: Float
        get() = 1.2f
}