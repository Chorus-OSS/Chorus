package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.utils.DyeColor

class ItemCyanDye : ItemDye(ItemID.Companion.CYAN_DYE) {
    override val dyeColor: DyeColor
        get() = DyeColor.CYAN
}