package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.network.protocol.types.BannerPatternType

/**
 * todo future
 */
class ItemFlowBannerPattern : ItemBannerPattern(ItemID.Companion.FLOW_BANNER_PATTERN) {
    override val patternType: BannerPatternType?
        get() =//todo future
            null

    override var damage: Int
        get() = super.damage
        set(damage) {
        }
}
