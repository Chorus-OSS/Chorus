package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.utils.DyeColor

class ItemBoneMeal : ItemDye(ItemID.BONE_MEAL) {
    override val isFertilizer: Boolean
        get() = true

    override val dyeColor: DyeColor
        get() = DyeColor.WHITE

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}