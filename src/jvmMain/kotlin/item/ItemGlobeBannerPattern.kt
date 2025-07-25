package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.network.protocol.types.BannerPatternType

class ItemGlobeBannerPattern : ItemBannerPattern(ItemID.Companion.GLOBE_BANNER_PATTERN) {
    override val patternType: BannerPatternType
        get() = BannerPatternType.GLOBE

    override var damage: Int
        get() = super.damage
        set(damage) {
        }
}