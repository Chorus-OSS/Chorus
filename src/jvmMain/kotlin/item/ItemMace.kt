package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.entity.Entity
import kotlin.math.floor

class ItemMace : ItemTool(ItemID.Companion.MACE) {
    override val maxDurability: Int
        get() = 501

    override fun getAttackDamage(entity: Entity): Int {
        val height = floor(entity.highestPosition - entity.position.y).toInt()
        if (height < 1.5f) return 6
        var damage = 0
        for (i in 0..height) {
            if (i < 3) damage += 4
            else if (i < 8) damage += 2
            else damage++
        }

        entity.resetFallDistance()
        return damage
    }
}