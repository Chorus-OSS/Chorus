package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.level.Locator
import org.chorus_oss.chorus.level.particle.WaxOffParticle
import org.chorus_oss.chorus.level.particle.WaxOnParticle
import org.chorus_oss.chorus.math.BlockFace

interface Waxable {
    val locator: Locator

    fun onActivate(item: Item, player: Player?, blockFace: BlockFace, fx: Float, fy: Float, fz: Float): Boolean {
        var waxed = isWaxed
        if ((item.id !== ItemID.HONEYCOMB || waxed) && (!item.isAxe || !waxed)) {
            return false
        }

        waxed = !waxed
        if (!setWaxed(waxed)) {
            return false
        }

        val location = if (this is Block) this else locator
        if (player == null || !player.isCreative) {
            if (waxed) {
                item.count--
            } else {
                item.useOn((if (this is Block) this else location.levelBlock))
            }
        }
        location.level.addParticle(if (waxed) WaxOnParticle(location.position) else WaxOffParticle(location.position))
        return true
    }

    val isWaxed: Boolean

    fun setWaxed(waxed: Boolean): Boolean
}
