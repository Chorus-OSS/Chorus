package org.chorus_oss.chorus.entity

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.math.Vector3

/**
 * An entity which can be named by name tags.
 */
interface EntityNameable {
    fun getNameTag(): String
    fun setNameTag(name: String)

    fun isNameTagVisible(): Boolean
    fun setNameTagVisible(value: Boolean)

    fun isPersistent(): Boolean
    fun setPersistent(persistent: Boolean)

    fun onInteract(player: Player, item: Item, clickedPos: Vector3): Boolean {
        if (item.id == ItemID.NAME_TAG && !player.isSpectator && (this !is Player)) {
            return playerApplyNameTag(player, item)
        }
        return false
    }

    fun playerApplyNameTag(player: Player, item: Item): Boolean {
        return playerApplyNameTag(player, item, true)
    }

    fun playerApplyNameTag(player: Player, item: Item, consume: Boolean): Boolean {
        if (item.hasCustomName()) {
            this.setNameTag(item.customName)
            this.setNameTagVisible(true)

            if (consume && !player.isCreative) {
                player.inventory.removeItem(item)
            }
            // Set entity as persistent.
            return true
        }
        return false
    }
}
