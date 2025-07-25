package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.entity.effect.EffectType
import org.chorus_oss.chorus.math.Vector3

class ItemHoneyBottle @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    ItemFood(ItemID.Companion.HONEY_BOTTLE, meta, count, "Honey Bottle") {
    override val maxStackSize: Int
        get() = 16

    override fun onClickAir(player: Player, directionVector: Vector3): Boolean {
        return true
    }

    override val foodRestore: Int
        get() = 6

    override val saturationRestore: Float
        get() = 1.2f

    override fun onEaten(player: Player): Boolean {
        player.inventory.addItem(ItemGlassBottle())
        player.removeEffect(EffectType.POISON)

        return true
    }
}
