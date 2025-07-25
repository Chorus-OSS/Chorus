package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.network.protocol.types.BannerPatternType

class ItemSkullBannerPattern : ItemBannerPattern(ItemID.Companion.SKULL_BANNER_PATTERN) {
    override val patternType: BannerPatternType
        get() = BannerPatternType.SKULL

    override var damage: Int
        get() = super.damage
        set(damage) {
        }
}