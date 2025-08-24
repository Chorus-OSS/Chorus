package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.network.protocol.types.BannerPatternType

class ItemPiglinBannerPattern : ItemBannerPattern(ItemID.PIGLIN_BANNER_PATTERN) {
    override val patternType: BannerPatternType
        get() = BannerPatternType.PIGLIN

    override var damage: Int
        get() = super.damage
        set(damage) {
        }
}