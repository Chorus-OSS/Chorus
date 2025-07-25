package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.math.Vector3

class ItemEmptyMap @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    Item(ItemID.Companion.EMPTY_MAP, meta, count, "Empty Map") {
    init {
        updateName()
    }

    override var damage: Int
        get() = super.damage
        set(meta) {
            super.damage = (meta)
            updateName()
        }

    private fun updateName() {
        name = if (damage == 2) {
            "Empty Locator Map"
        } else {
            "Empty Map"
        }
    }

    override fun onClickAir(player: Player, directionVector: Vector3): Boolean {
        if (!player.isCreative) {
            count--
        }
        player.inventory.addItem(get(ItemID.Companion.FILLED_MAP))
        return true
    }
}
