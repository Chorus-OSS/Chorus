package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.network.protocol.types.BannerPatternType

class ItemFlowerBannerPattern : ItemBannerPattern(ItemID.Companion.FLOWER_BANNER_PATTERN) {
    override val patternType: BannerPatternType
        get() = BannerPatternType.FLOWER

    override var damage: Int
        get() = super.damage
        set(damage) {
        }
}